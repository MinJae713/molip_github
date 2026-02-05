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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {
  private final SimpMessagingTemplate messagingTemplate;
  private final QuizService quizService;
  private final StringRedisTemplate redisTemplate;
  private final ExitQuizService exitQuizService;

  @MessageMapping("/quiz/{roomId}")
  public void makeQuiz(@DestinationVariable String roomId) {
    AiQuizMessage aiQuizMessage = quizService.createQuizRedis(Long.parseLong(roomId), 200);
    messagingTemplate.convertAndSend("/topic/quiz/" + roomId, aiQuizMessage);
  }

  @MessageMapping("/quiz/{roomId}/test")
  public void makeQuizTest(@DestinationVariable String roomId) {
    AiQuizMessage aiQuizMessage = quizService.createQuizRedisTest(Long.parseLong(roomId));
    messagingTemplate.convertAndSend("/topic/quiz/" + roomId, aiQuizMessage);
  }

  @MessageMapping("/quiz/send/{roomId}")
  public void sendQuiz(@DestinationVariable String roomId, AiQuizMessage quizMessage) {
    messagingTemplate.convertAndSend("/topic/surprise-quiz/" + roomId, quizMessage);
  }

  @GetMapping("/db")
  public ResponseEntity<AiQuizMessage> getAiQuizMysql(
      @RequestParam("session-id") Long sessionId,
      @RequestParam("timeline") int timeline,
      @RequestParam("text-length") int textLength) {
    AiQuizMessage quiz = quizService.createQuizDB(sessionId, timeline, textLength);
    return ResponseEntity.status(HttpStatus.OK).body(quiz);
  }

  @GetMapping("/live")
  public ResponseEntity<?> getAiQuizRedis(
      @RequestParam("session-id") Long sessionId, @RequestParam("text-length") int textLength) {
    if (redisTemplate.hasKey("quiz:stt:room-id:" + sessionId) == Boolean.FALSE) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "Redis에 해당 세션의 데이터가 없습니다."));
    }

    AiQuizMessage quiz = quizService.createQuizRedis(sessionId, textLength);
    return ResponseEntity.status(HttpStatus.OK).body(quiz);
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
