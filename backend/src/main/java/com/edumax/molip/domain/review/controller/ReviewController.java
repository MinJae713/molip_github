package com.edumax.molip.domain.review.controller;

import com.edumax.molip.domain.quiz.dto.response.AiQuizMessage;
import com.edumax.molip.domain.quiz.service.QuizService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
  private final QuizService quizService;

  // TODO: Redis 스크립트 결과 저장 테스트 필요
  @PostMapping("/save/script")
  public ResponseEntity<Map<String, String>> saveScriptResult() {
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Redis 스크립트 DB 저장 완료"));
  }

  @GetMapping("/ai-quiz")
  public ResponseEntity<AiQuizMessage> getAiQuiz(
      @RequestParam Long sessionId, @RequestParam int time) {
    AiQuizMessage quiz = quizService.createQuizDB(sessionId, time, 200);
    return ResponseEntity.status(HttpStatus.OK).body(quiz);
  }
}
