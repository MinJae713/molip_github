package com.edumax.molip.domain.review.controller;

import com.edumax.molip.domain.quiz.dto.response.AiQuizMessage;
import com.edumax.molip.domain.quiz.service.QuizService;
import com.edumax.molip.domain.review.entity.TimelineElement;
import com.edumax.molip.domain.review.service.ReviewService;
import com.edumax.molip.domain.review.service.StorageService;
import com.edumax.molip.domain.session.dto.request.EndAnalysisRequest;
import java.util.List;
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
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
  private final QuizService quizService;
  private final ReviewService reviewService;
  private final StorageService storageService;

  @PostMapping("/test")
  public ResponseEntity<?> testEndpoint() {
    List<TimelineElement> testMergedList =
        List.of(
            new TimelineElement(
                1000, // timeline (ms)
                false, // empty
                0.85f, // concentrateValue
                false, // fever
                new float[] {0.7f, 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.2f} // emotionValueList
                ),
            new TimelineElement(
                2000,
                true, // 자리를 비운 상태
                0.0f,
                false,
                new float[] {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}),
            new TimelineElement(
                3000, false, 0.62f, false, new float[] {0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.9f}),
            new TimelineElement(
                4000,
                false,
                0.98f, // 매우 높은 집중
                true, // Fever 상태 (몰입 절정)
                new float[] {0.4f, 0.0f, 0.0f, 0.6f, 0.0f, 0.0f, 0.0f}));
    storageService.saveTimelineAnalysis(1L, 1L, testMergedList);
    List<TimelineElement> retrievedList = storageService.getTimelineAnalysis(1L, 1L);

    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", retrievedList));
  }

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
