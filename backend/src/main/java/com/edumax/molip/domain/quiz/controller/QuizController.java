package com.edumax.molip.domain.quiz.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest;
import com.edumax.molip.domain.quiz.dto.request.SubmitExitQuizRequest;
import com.edumax.molip.domain.quiz.dto.response.AiQuizMessage;
import com.edumax.molip.domain.quiz.dto.response.CreateExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.FindExitQuizResponse;
import com.edumax.molip.domain.quiz.dto.response.SubmitExitQuizListResponse;
import com.edumax.molip.domain.quiz.service.ExitQuizService;
import com.edumax.molip.domain.quiz.service.QuizService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {
  private final SimpMessagingTemplate messagingTemplate;
  private final QuizService quizService;
  private final ExitQuizService exitQuizService;

  @MessageMapping("/quiz/{roomId}")
  public void makeQuiz(@DestinationVariable String roomId) {
    AiQuizMessage aiQuizMessage = quizService.createQuizRedis(roomId, 100);
    messagingTemplate.convertAndSend("/topic/quiz/" + roomId, aiQuizMessage);
  }

  @MessageMapping("/quiz/send/{roomId}")
  public void sendQuiz(@DestinationVariable String roomId, AiQuizMessage quizMessage) {
    messagingTemplate.convertAndSend("/topic/surprise-quiz/" + roomId, quizMessage);
  }

  @PostMapping("/{sessionId}/exit")
  public ResponseEntity<CreateExitQuizResponse> createExitQuiz(
      @PathVariable Long sessionId,
      @AuthenticationPrincipal MemberUserDetails member,
      @Valid @RequestBody List<CreateExitQuizRequest> createExitQuizRequest) {
    return ResponseEntity.ok(
        exitQuizService.createExitQuiz(sessionId, member.getMemberId(), createExitQuizRequest));
  }

  @GetMapping("{sessionId}/exit")
  public ResponseEntity<List<FindExitQuizResponse>> findExitQuizList(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(exitQuizService.findExitQuizList(sessionId));
  }

  @PostMapping("/{sessionId}/exit/submit")
  public ResponseEntity<List<SubmitExitQuizListResponse>> submitExitQuizList(
      @PathVariable Long sessionId,
      @AuthenticationPrincipal MemberUserDetails member,
      @Valid @RequestBody List<SubmitExitQuizRequest> submitExitQuizListRequest) {
    return ResponseEntity.ok(
        exitQuizService.submitExitQuizList(
            sessionId, member.getMemberId(), submitExitQuizListRequest));
  }
}
