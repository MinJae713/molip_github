package com.edumax.molip.domain.review.controller;

import com.edumax.molip.domain.quiz.dto.response.AiQuizMessage;
import com.edumax.molip.domain.quiz.service.QuizService;
import com.edumax.molip.domain.review.service.ReviewService;
import com.edumax.molip.domain.session.dto.request.EndAnalysisRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
  private final QuizService quizService;
  private final ReviewService reviewService;

  @PostMapping("/start/analysis/{sessionId}")
  public ResponseEntity<Map<String, String>> startAnalysis(@PathVariable String sessionId) {
    reviewService.startAnalysis(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "분석 시작"));
  }

  @PostMapping("/end/analysis/{sessionId}")
  public ResponseEntity<?> endAnalysis(
      @PathVariable String sessionId, @RequestBody(required = false) EndAnalysisRequest request) {
    reviewService.endAnalysis(sessionId, request);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "분석 종료"));
  }

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
