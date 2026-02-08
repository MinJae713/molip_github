package com.edumax.molip.domain.review.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.common.util.JacksonUtil;
import com.edumax.molip.domain.quiz.service.QuizService;
import com.edumax.molip.domain.review.dto.response.ScriptResponse;
import com.edumax.molip.domain.review.entity.EmotionGraph;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
import com.edumax.molip.domain.review.entity.TimelineElement;
import com.edumax.molip.domain.review.service.ReviewService;
import com.edumax.molip.domain.review.service.StorageService;
import com.edumax.molip.domain.session.dto.request.EndAnalysisRequest;
import com.edumax.molip.domain.session.dto.request.PauseElement;
import com.edumax.molip.domain.session.dto.response.AnalysisSignalMessage;
import com.edumax.molip.domain.session.dto.response.SessionDetailResponse;
import com.edumax.molip.domain.session.entity.CourseLearnerSession;
import com.edumax.molip.domain.session.handler.AnalysisManager;
import com.edumax.molip.domain.session.service.SessionService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.type.TypeReference;

@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
  private final QuizService quizService;
  private final ReviewService reviewService;
  private final StorageService storageService;
  private final SessionService sessionService;

  private float deadzoneScale = 0.5f;

  public static String toEmotion(int index) {
    return switch (index) {
      case 0 -> "happy";
      case 1 -> "sad";
      case 2 -> "angry";
      case 3 -> "surprise";
      case 4 -> "fear";
      case 5 -> "disgust";
      case 6 -> "neutral";
      default -> "unknown";
    };
  }

  @PostMapping("/start/analysis/{sessionId}")
  public ResponseEntity<Map<String, String>> startAnalysis(@PathVariable String sessionId) {
    AnalysisSignalMessage analysisSignalMessage = reviewService.startAnalysis(sessionId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(Map.of("message", "분석 시작", "startTime", analysisSignalMessage.startTime()));
  }

  @PostMapping("/end/analysis/{sessionId}")
  public ResponseEntity<?> endAnalysis(
      @PathVariable String sessionId,
      @RequestBody(required = false) EndAnalysisRequest request,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    reviewService.endAnalysis(sessionId, request, userDetails.getMemberId());
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "분석 종료"));
  }

  @GetMapping("/timeline/learner/{sessionId}")
  public ResponseEntity<LearnerTimelineResponse> getLearnerTimelineAnalysis(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    SessionDetailResponse sessionDetail =
        sessionService.getSessionDetail(sessionId, userDetails.getMemberId());

    LearnerAnalysis sessionAnalysis = reviewService.getLearnerAnalysis(sessionId);
    List<PauseElement> pauseElementList =
        JacksonUtil.fromJson(
            sessionAnalysis.getPauseTime(), new TypeReference<List<PauseElement>>() {});

    CourseLearnerSession learnerAnalysis =
        reviewService.getLearnerSessionAnalysis(sessionId, userDetails.getMemberId());

    List<TimelineElement> learnerTimelineData =
        storageService.getTimelineAnalysis(sessionId, userDetails.getMemberId());
    List<TimelineElement> trainerTimelineData =
        storageService.getTimelineAnalysis(sessionId, sessionDetail.getTrainerId());

    if (learnerAnalysis == null) {
      return ResponseEntity.status(HttpStatus.OK)
          .body(
              new LearnerTimelineResponse(
                  learnerAnalysis.getAvgConcentrate() * deadzoneScale,
                  pauseElementList,
                  learnerTimelineData,
                  trainerTimelineData));
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new LearnerTimelineResponse(
                learnerAnalysis.getAvgConcentrate() * deadzoneScale,
                pauseElementList,
                learnerTimelineData,
                trainerTimelineData));
  }

  record LearnerTimelineResponse(
      float deadzonePercent,
      List<PauseElement> pauseElementList,
      List<TimelineElement> learnerTimelineData,
      List<TimelineElement> trainerTimelineData) {}

  @GetMapping("/timeline/trainer/{sessionId}")
  public ResponseEntity<TrainerTimelineResponse> getTeacherTimelineAnalysis(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {

    LearnerAnalysis learnerAnalysis = reviewService.getLearnerAnalysis(sessionId);
    List<PauseElement> pauseElementList =
        JacksonUtil.fromJson(
            learnerAnalysis.getPauseTime(), new TypeReference<List<PauseElement>>() {});

    SessionDetailResponse sessionDetail =
        sessionService.getSessionDetail(sessionId, userDetails.getMemberId());
    List<TimelineElement> trainerTimelineData =
        storageService.getTimelineAnalysis(sessionId, sessionDetail.getTrainerId());

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new TrainerTimelineResponse(
                learnerAnalysis.getAvgConcentrate() * deadzoneScale,
                pauseElementList,
                trainerTimelineData));
  }

  record TrainerTimelineResponse(
      float deadzonePercent,
      List<PauseElement> pauseElementList,
      List<TimelineElement> trainerTimelineData) {}

  @GetMapping("/emotion-graph/learner/{sessionId}")
  public ResponseEntity<LearnerEmotionGraphResponse> getLearnerEmotionGraphData(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    // 1. 본인(Learner) 데이터 조회 및 가공
    List<EmotionGraphResponse> learnerResponseList =
        processEmotionData(reviewService.getEmotionGraph(sessionId, userDetails.getMemberId()));

    // 2. 상대방(Trainer) 데이터 조회를 위한 ID 획득
    SessionDetailResponse sessionDetail =
        sessionService.getSessionDetail(sessionId, userDetails.getMemberId());

    // 3. 상대방 데이터 조회 및 가공
    List<EmotionGraphResponse> trainerResponseList =
        processEmotionData(reviewService.getEmotionGraph(sessionId, sessionDetail.getTrainerId()));

    return ResponseEntity.ok(
        new LearnerEmotionGraphResponse(learnerResponseList, trainerResponseList));
  }

  record LearnerEmotionGraphResponse(
      List<EmotionGraphResponse> learnerEmotionGraph,
      List<EmotionGraphResponse> trainerEmotionGraph) {}

  @GetMapping("/emotion-graph/trainer/{sessionId}")
  public ResponseEntity<TrainerEmotionGraphResponse> getTrainerEmotionGraphData(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    List<EmotionGraphResponse> trainerResponseList =
        processEmotionData(reviewService.getEmotionGraph(sessionId, userDetails.getMemberId()));

    return ResponseEntity.ok(new TrainerEmotionGraphResponse(trainerResponseList));
  }

  record TrainerEmotionGraphResponse(List<EmotionGraphResponse> trainerEmotionGraph) {}

  private List<EmotionGraphResponse> processEmotionData(List<EmotionGraph> emotionGraphList) {
    return emotionGraphList.stream()
        .map(
            cur ->
                new EmotionGraphResponse(
                    AnalysisManager.getEmotionIndex(cur.getName()), cur.getName(), cur.getValue()))
        .sorted(Comparator.comparing(EmotionGraphResponse::index))
        .limit(7)
        .toList();
  }

  record EmotionGraphResponse(Integer index, String name, Float value) {}

  @GetMapping("/script/{sessionId}")
  public ResponseEntity<List<ScriptResponse>> getSessionScript(@PathVariable Long sessionId) {
    List<ScriptResponse> scriptList = reviewService.getScripts(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(scriptList);
  }

  @GetMapping("/script/{sessionId}/test")
  public ResponseEntity<List<ScriptResponse>> getSessionScriptTest(@PathVariable Long sessionId) {
    List<ScriptResponse> scriptList = reviewService.getScripts(1L);
    return ResponseEntity.status(HttpStatus.OK).body(scriptList);
  }
}
