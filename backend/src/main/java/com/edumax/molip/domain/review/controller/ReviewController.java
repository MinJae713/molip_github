package com.edumax.molip.domain.review.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.quiz.service.QuizService;
import com.edumax.molip.domain.review.entity.EmotionGraph;
import com.edumax.molip.domain.review.entity.TimelineElement;
import com.edumax.molip.domain.review.service.ReviewService;
import com.edumax.molip.domain.review.service.StorageService;
import com.edumax.molip.domain.session.dto.request.EndAnalysisRequest;
import com.edumax.molip.domain.session.dto.response.SessionDetailResponse;
import com.edumax.molip.domain.session.handler.AnalysisManager;
import com.edumax.molip.domain.session.service.SessionService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
  private final QuizService quizService;
  private final ReviewService reviewService;
  private final StorageService storageService;
  private final SessionService sessionService;

  public static List<TimelineElement> generateData(int count) {
    List<TimelineElement> list = new ArrayList<>();
    Random random = new Random();

    float currentVal = 30.0f; // 시작값
    float[] steps = {-0.25f, -0.125f, 0.0f, 0.125f, 0.25f};

    for (int i = 0; i < count; i++) {
      // 1. concentrateValue 계산
      // 랜덤하게 증감 단위를 선택하고 0~100 범위를 유지함
      float step = steps[random.nextInt(steps.length)];
      currentVal = Math.max(0, Math.min(100, currentVal + step));

      // 2. empty 여부 (예: 5% 확률로 데이터가 비어있음)
      boolean isEmpty = random.nextFloat() < 0.05;

      // 3. 요구사항: empty가 true일 때는 concentrateValue가 없어야 함 (0.0f로 설정)
      float finalVal = 0.0f;

      if (i > 0) {
        finalVal = isEmpty ? list.get(i - 1).getConcentrateValue() : currentVal;
      } else {
        finalVal = isEmpty ? 0.0f : currentVal;
      }

      // 4. fever 여부 (랜덤)
      boolean isFever = false;
      if (!isEmpty) {
        isFever = random.nextBoolean();
      }

      // 5. 감정 인덱스 및 이름 매칭
      int emotionIndex = random.nextInt(7);
      String emotionName = toEmotion(emotionIndex);

      list.add(new TimelineElement(i, isEmpty, finalVal, isFever, emotionIndex, emotionName));
    }
    return list;
  }

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

  @PostMapping("/test/{sessionId}/{memberId}")
  public ResponseEntity<LearnerTimelineResponse> testEndpoint(
      @PathVariable Long sessionId,
      @PathVariable Long memberId,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    //    List<TimelineElement> testMergedList = generateData(3600);
    //
    //    storageService.saveTimelineAnalysis(10002L, 10002L, testMergedList);
    //
    //    testMergedList = generateData(3600);
    //
    //    storageService.saveTimelineAnalysis(10001L, 10001L, testMergedList);
    List<TimelineElement> learnerTimelineData =
        storageService.getTimelineAnalysis(sessionId, memberId);
    List<TimelineElement> trainerTimelineData = storageService.getTimelineAnalysis(10001L, 10001L);
    return ResponseEntity.status(HttpStatus.OK)
        .body(new LearnerTimelineResponse(learnerTimelineData, trainerTimelineData));
  }

  @PostMapping("/start/analysis/{sessionId}")
  public ResponseEntity<Map<String, String>> startAnalysis(@PathVariable String sessionId) {
    reviewService.startAnalysis(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "분석 시작"));
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
    List<TimelineElement> learnerTimelineData =
        storageService.getTimelineAnalysis(sessionId, userDetails.getMemberId());
    List<TimelineElement> trainerTimelineData =
        storageService.getTimelineAnalysis(sessionId, sessionDetail.getTrainerId());
    return ResponseEntity.status(HttpStatus.OK)
        .body(new LearnerTimelineResponse(learnerTimelineData, trainerTimelineData));
  }

  record LearnerTimelineResponse(
      List<TimelineElement> learnerTimelineData, List<TimelineElement> TrainerTimelineData) {}

  @GetMapping("/timeline/trainer/{sessionId}")
  public ResponseEntity<TrainerTimelineResponse> getTeacherTimelineAnalysis(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    SessionDetailResponse sessionDetail =
        sessionService.getSessionDetail(sessionId, userDetails.getMemberId());
    List<TimelineElement> trainerTimelineData =
        storageService.getTimelineAnalysis(sessionId, sessionDetail.getTrainerId());
    return ResponseEntity.status(HttpStatus.OK)
        .body(new TrainerTimelineResponse(trainerTimelineData));
  }

  record TrainerTimelineResponse(List<TimelineElement> TrainerTimelineData) {}

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
}
