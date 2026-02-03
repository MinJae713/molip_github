package com.edumax.molip.domain.review.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseLearnerAnalysis;
import com.edumax.molip.domain.course.repository.CourseLearnerAnalysisRepository;
import com.edumax.molip.domain.course.repository.CourseRepository;
import com.edumax.molip.domain.review.entity.EmotionGraph;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
import com.edumax.molip.domain.review.entity.Script;
import com.edumax.molip.domain.review.entity.TimelineElement;
import com.edumax.molip.domain.review.repository.EmotionGraphRepository;
import com.edumax.molip.domain.review.repository.LearnerAnalysisRepository;
import com.edumax.molip.domain.review.repository.ScriptRepository;
import com.edumax.molip.domain.session.dto.request.EndAnalysisRequest;
import com.edumax.molip.domain.session.dto.request.PauseElement;
import com.edumax.molip.domain.session.dto.response.AnalysisSignalMessage;
import com.edumax.molip.domain.session.entity.CourseLearnerSession;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.session.handler.AnalysisManager;
import com.edumax.molip.domain.session.handler.AnalysisManagerRegistry;
import com.edumax.molip.domain.session.handler.EmotionSaveListener;
import com.edumax.molip.domain.session.repository.CourseLearnerSessionRepository;
import com.edumax.molip.domain.session.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final SimpMessagingTemplate messagingTemplate;
  private final StringRedisTemplate redisTemplate;

  private final AnalysisManagerRegistry analysisManagerRegistry;
  private final EmotionSaveListener emotionSaveListener;

  private final SessionRepository sessionRepository;
  private final ScriptRepository scriptRepository;
  private final LearnerAnalysisRepository learnerAnalysisRepository;
  private final CourseLearnerSessionRepository courseLearnerSessionRepository;
  private final EmotionGraphRepository emotionGraphRepository;
  private final CourseLearnerAnalysisRepository courseLearnerAnalysisRepository;
  private final CourseRepository courseRepository;
  private final StorageService storageService;

  public List<EmotionGraph> getEmotionGraph(Long sessionId, Long memberId) {
    return emotionGraphRepository.findBySessionIdAndMemberId(sessionId, memberId);
  }

  public void startAnalysis(String sessionId) {
    emotionSaveListener.startLecture(sessionId);
    AnalysisManager analysisManager = analysisManagerRegistry.getManager(sessionId);
    analysisManager.startScheduling(0, sessionId);

    // 모든 참여자에게 분석 시작 시간 보내기
    String joinKey = getJoinKey(sessionId);
    Set<String> memberList = redisTemplate.opsForSet().members(joinKey);
    List<String> memberIdList = new ArrayList<>();
    if (memberList != null) {
      for (String member : memberList) {
        String[] parts = member.split("\\|", 2);
        if (parts.length == 2) {
          memberIdList.add(parts[0]);
        }
      }
    }
    sendAnalysisMessage(memberIdList, sessionId);
  }

  private void sendAnalysisMessage(List<String> memberIdList, String sessionId) {
    AnalysisSignalMessage analysisSignalMessage =
        new AnalysisSignalMessage(LocalDateTime.now().toString());
    for (String memberId : memberIdList) {
      messagingTemplate.convertAndSend(
          "/topic/analysis/" + sessionId + "/" + memberId, analysisSignalMessage);
    }
    messagingTemplate.convertAndSend("/topic/analysis/" + sessionId, analysisSignalMessage);
  }

  private int getRestTime(List<PauseElement> pauseList) {
    int restTime = 0;
    for (PauseElement pause : pauseList) {
      restTime += pause.endTime() - pause.startTime();
    }
    return restTime;
  }

  @Transactional
  public void endAnalysis(String sessionId, EndAnalysisRequest request, Long trainerId) {
    emotionSaveListener.stopLecture(sessionId);
    analysisManagerRegistry.removeManager(sessionId);

    Long sessionIdLong = Long.parseLong(sessionId);
    String joinKey = getJoinKey(sessionId);

    List<Long> memberIdList =
        redisTemplate.opsForSet().members(joinKey).stream()
            .map(entry -> Long.parseLong(entry.split("\\|", 2)[0]))
            .filter(memberId -> !trainerId.equals(memberId))
            .toList();

    boolean studentZero = false;
    Long studentSizeTmp = redisTemplate.opsForSet().size(joinKey) - 1;
    if (studentSizeTmp <= 0) {
      studentZero = true;
      studentSizeTmp = 1L;
    }
    final Long studentSize = studentSizeTmp;

    boolean totalTimelineZero = false;
    int totalTimeline = request.getLastTimeline() - getRestTime(request.getPauseElementList());
    if (totalTimeline <= 0) {
      totalTimelineZero = true;
      totalTimeline = 1;
    }

    Session session =
        sessionRepository
            .findById(sessionIdLong)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

    Course course = session.getCourse();
    int attendance = course.getAttendance();
    int endSessionCnt = course.getEndSessionCount() + 1;

    // stt 데이터 저장
    saveScriptToMysql(sessionIdLong);

    // 1. 몰입도 데이터 Pipeline 실행
    List<Object> redisConResult =
        redisTemplate.executePipelined(
            (RedisCallback<?>)
                connection -> {
                  for (Long memberId : memberIdList) {
                    String concentrateKey =
                        "review:concentrate:session-id:%s:member-id:%d"
                            .formatted(sessionId, memberId);
                    connection.listCommands().lRange(concentrateKey.getBytes(), 0, -1);
                    connection.keyCommands().del(concentrateKey.getBytes());
                  }
                  return null;
                });

    // 2. 감정 데이터 Pipeline 실행
    List<Object> redisEmoResult =
        redisTemplate.executePipelined(
            (RedisCallback<?>)
                connection -> {
                  for (Long memberId : memberIdList) {
                    String emotionKey =
                        "review:emotion:session-id:%s:member-id:%d".formatted(sessionId, memberId);
                    connection.listCommands().lRange(emotionKey.getBytes(), 0, -1);
                    connection.keyCommands().del(emotionKey.getBytes());
                  }
                  return null;
                });

    Map<Long, Long> attendanceCntList =
        courseLearnerSessionRepository.findAllBySessionSessionId(sessionIdLong).stream()
            .filter(CourseLearnerSession::getIsAttendance)
            .collect(
                Collectors.groupingBy(CourseLearnerSession::getMemberId, Collectors.counting()));

    List<CourseLearnerSession> sessionResultsToSave = new ArrayList<>();
    List<CourseLearnerAnalysis> existingAnalyses =
        courseLearnerAnalysisRepository.findAllByCourseAndMemberIdList(course, memberIdList);

    Map<Long, CourseLearnerAnalysis> analysisMap =
        existingAnalyses.stream()
            .collect(Collectors.toMap(CourseLearnerAnalysis::getMemberId, ele -> ele));

    List<CourseLearnerAnalysis> courseResultsToSave = new ArrayList<>();
    List<EmotionGraph> emotionGraphsToSave = new ArrayList<>();
    float totalSumQuizCorrenct = 0f;
    float totalSumConcentrate = 0f;
    float totalSumAttendance = 0f;
    float[] totalSumEmotion = new float[7];

    // --- [추가된 강사 집계용 저장소] ---
    // Key: timeline, Value: [concentrateSum, feverCount, emptyCount, emoSum0, emoSum1 ... emoSum6]
    // (총 10개 공간)
    Map<Integer, float[]> trainerAggregationMap = new TreeMap<>();

    // 멤버별 통합 처리 루프
    for (int i = 0; i < memberIdList.size(); i++) {
      Long memberId = memberIdList.get(i);
      List<String> rawConList = (List<String>) redisConResult.get(i * 2);
      List<String> rawEmoList = (List<String>) redisEmoResult.get(i * 2);

      if (rawConList == null || rawConList.isEmpty()) {
        continue;
      }

      // conList 계산
      List<ConcentrateElement> conList =
          rawConList.stream()
              .map(
                  s -> {
                    int timeline = Integer.parseInt(s.split("\\|")[0]);
                    String[] parts = s.split("\\|")[1].split(",");
                    return new ConcentrateElement(
                        timeline,
                        Boolean.parseBoolean(parts[0]),
                        Float.parseFloat(parts[1]),
                        Boolean.parseBoolean(parts[2]));
                  })
              .sorted(Comparator.comparingInt(ConcentrateElement::timeline))
              .toList();

      // emoList 계산
      List<EmotionElement> emoList =
          (rawEmoList == null)
              ? new ArrayList<>()
              : rawEmoList.stream()
                  .map(
                      s -> {
                        int timeline = Integer.parseInt(s.split("\\|")[0]);
                        String[] parts = s.split("\\|")[1].split(",");
                        EmotionElement result = new EmotionElement(timeline, new float[7]);
                        for (String part : parts) {
                          String name = part.split(":", 2)[0];
                          float value = Float.parseFloat(part.split(":", 2)[1]);
                          result.emotionValueList[AnalysisManager.getEmotionIndex(name)] = value;
                        }
                        return result;
                      })
                  .sorted(Comparator.comparingInt(EmotionElement::getTimeline))
                  .toList();

      Map<Integer, EmotionElement> emoMap =
          emoList.stream()
              .collect(Collectors.toMap(EmotionElement::getTimeline, e -> e, (v1, v2) -> v1));

      List<TimelineElement> mergedList =
          conList.stream()
              .map(
                  con -> {
                    EmotionElement emo = emoMap.get(con.timeline());
                    if (emo == null) {
                      return null;
                    }

                    // --- 강사 데이터 집계 ---
                    float[] agg =
                        trainerAggregationMap.computeIfAbsent(con.timeline(), k -> new float[10]);
                    agg[0] += con.concentrateValue(); // 몰입도 합
                    agg[1] += (con.fever() ? 1 : 0); // Fever 수
                    agg[2] += (con.empty() ? 1 : 0); // Empty 수
                    for (int e = 0; e < 7; e++) {
                      agg[3 + e] += emo.emotionValueList[e]; // 감정 7개 합
                    }
                    // -----------------------

                    // 1등 감정 찾기 (학생 개별용)
                    int maxIdx = 0;
                    float maxVal = emo.emotionValueList[0];
                    for (int j = 1; j < 7; j++) {
                      if (emo.emotionValueList[j] > maxVal) {
                        maxVal = emo.emotionValueList[j];
                        maxIdx = j;
                      }
                    }
                    return new TimelineElement(
                        con.timeline(),
                        con.empty(),
                        con.concentrateValue(),
                        con.fever(),
                        maxIdx,
                        AnalysisManager.toEmotion(maxIdx));
                  })
              .filter(Objects::nonNull)
              .toList();

      storageService.saveTimelineAnalysis(sessionIdLong, memberId, mergedList);

      // 기존 통계 계산 (Concentrate)
      int attCnt = 0;
      float conSum = 0f;
      for (ConcentrateElement ele : conList) {
        attCnt += (ele.empty() ? 0 : 1);
        conSum += ele.concentrateValue();
      }

      boolean isAttendance = attCnt * 100 / totalTimeline >= attendance;
      float avgCon = conSum / totalTimeline;
      totalSumConcentrate += avgCon;

      sessionResultsToSave.add(
          CourseLearnerSession.builder()
              .isAttendance(isAttendance)
              .attendanceTime(attCnt)
              .avgConcentrate(avgCon)
              .session(session)
              .memberId(memberId)
              .build());

      CourseLearnerAnalysis analysis =
          analysisMap.getOrDefault(
              memberId,
              CourseLearnerAnalysis.builder()
                  .course(course)
                  .memberId(memberId)
                  .sumConcentrate(0f)
                  .sumAttendance(0)
                  .sumQuizCorrect(0f)
                  .build());
      analysis.updateAnalysis(avgCon, isAttendance, 0.0f);
      courseResultsToSave.add(analysis);

      long totalAttCount = attendanceCntList.getOrDefault(memberId, 0L) + (isAttendance ? 1 : 0);
      totalSumAttendance += (float) totalAttCount / endSessionCnt * 100;

      // 기존 통계 계산 (Emotion)
      float[] emoSum = new float[7];
      for (EmotionElement ele : emoList) {
        for (int e = 0; e < 7; e++) {
          emoSum[e] += ele.emotionValueList[e];
        }
      }
      for (int e = 0; e < 7; e++) {
        float avgEmo = emoSum[e] / totalTimeline;
        totalSumEmotion[e] += avgEmo;
        emotionGraphsToSave.add(
            EmotionGraph.builder()
                .name(AnalysisManager.toEmotion(e))
                .value(avgEmo)
                .learnerAnalysis(null)
                .memberId(memberId)
                .build());
      }
    }

    // --- [추가된 강사 종합 TrainerTimelineElement 생성 및 저장] ---
    List<TimelineElement> trainerMergedList =
        trainerAggregationMap.entrySet().stream()
            .map(
                entry -> {
                  int tl = entry.getKey();
                  float[] agg = entry.getValue();

                  // 감정 7개 합 중 최대값 인덱스 찾기 (agg[3] ~ agg[9])
                  int maxIdx = 0;
                  float maxVal = agg[3];
                  for (int j = 1; j < 7; j++) {
                    if (agg[3 + j] > maxVal) {
                      maxVal = agg[3 + j];
                      maxIdx = j;
                    }
                  }

                  return new TimelineElement(
                      tl,
                      agg[2] >= studentSize, // 전원 부재 시 empty
                      agg[0] / studentSize, // 평균 몰입도
                      agg[1] > (studentSize / 2.0), // 과반수 이상 fever 시 fever
                      maxIdx,
                      AnalysisManager.toEmotion(maxIdx));
                })
            .toList();

    storageService.saveTimelineAnalysis(sessionIdLong, trainerId, trainerMergedList);

    // LearnerAnalysis 저장 및 감정 그래프 연결
    LearnerAnalysis learnerAnalysis =
        learnerAnalysisRepository.save(
            LearnerAnalysis.builder()
                .session(session)
                .totalLearner(studentZero ? 0 : studentSize.intValue())
                .totalAnalysisTime(totalTimelineZero ? 0 : totalTimeline)
                .avgAttendance(totalSumAttendance / studentSize)
                .avgConcentrate(totalSumConcentrate / studentSize)
                .avgQuizCorrect(totalSumQuizCorrenct / studentSize)
                .build());

    List<EmotionGraph> finalEmotions =
        emotionGraphsToSave.stream()
            .map(
                eg ->
                    EmotionGraph.builder()
                        .name(eg.getName())
                        .value(eg.getValue())
                        .learnerAnalysis(learnerAnalysis)
                        .memberId(eg.getMemberId())
                        .build())
            .collect(Collectors.toCollection(ArrayList::new));

    for (int e = 0; e < 7; e++) {
      finalEmotions.add(
          EmotionGraph.builder()
              .name(AnalysisManager.toEmotion(e))
              .value(totalSumEmotion[e] / studentSize)
              .learnerAnalysis(learnerAnalysis)
              .memberId(trainerId)
              .build());
    }

    // 최종 DB 저장 및 통계 업데이트
    emotionGraphRepository.saveAll(finalEmotions);
    courseLearnerSessionRepository.saveAll(sessionResultsToSave);
    courseLearnerAnalysisRepository.saveAll(courseResultsToSave);
    courseLearnerAnalysisRepository.flush();

    // 강의 정보 업데이트 로직 (기존 코드 유지)
    updateCourseStatistics(course, endSessionCnt);
  }

  private void updateCourseStatistics(Course course, int endSessionCnt) {
    Long courseStudentCnt =
        courseLearnerAnalysisRepository.countByCourseCourseId(course.getCourseId());
    if (courseStudentCnt == 0) {
      courseStudentCnt = 1L;
    }
    List<Object[]> resultList =
        courseLearnerAnalysisRepository.findSumByCourseId(course.getCourseId());
    if (resultList != null && !resultList.isEmpty()) {
      Object[] row = resultList.get(0);
      float totalSumAtt = (row[0] != null) ? ((Number) row[0]).floatValue() : 0f;
      float totalSumCon = (row[1] != null) ? ((Number) row[1]).floatValue() : 0f;
      float totalSumQuiz = (row[2] != null) ? ((Number) row[2]).floatValue() : 0f;
      float denominator = (float) endSessionCnt * courseStudentCnt;
      course.setAvgAttendance(totalSumAtt / denominator * 100f);
      course.setAvgConcentrate(totalSumCon / denominator);
      course.setAvgQuizCorrect(totalSumQuiz / denominator);
    }
    course.setEndSessionCount(endSessionCnt);
    courseRepository.save(course);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  private class EmotionElement {
    int timeline;
    float[] emotionValueList;
  }

  record ConcentrateElement(int timeline, boolean empty, float concentrateValue, boolean fever) {}

  @Transactional
  public void saveScriptToMysql(Long sessionId) {
    String key = generateKey(sessionId);
    Set<String> values = redisTemplate.opsForZSet().range(key, 0, -1);

    if (values == null || values.isEmpty()) {
      return;
    }

    // DB를 조회하지 않고도 연관관계를 맺기 위함입니다.
    LearnerAnalysis learnerAnalysis = LearnerAnalysis.builder().sessionId(sessionId).build();

    List<Script> scriptList =
        values.stream()
            .map(
                val -> {
                  String[] parts = val.split("\\|", 2);

                  return Script.builder()
                      .timeline(Integer.parseInt(parts[0]))
                      .message(parts.length > 1 ? parts[1] : "")
                      .learnerAnalysis(learnerAnalysis) // 위에서 만든 객체 삽입
                      .build();
                })
            .toList();

    scriptRepository.saveAll(scriptList);

    redisTemplate.delete(key);
  }

  String QUIZ_STT_FORMAT = "quiz:stt:session-id:%d";

  String generateKey(Long sessionId) {
    return QUIZ_STT_FORMAT.formatted(sessionId);
  }

  String SESSION_JOIN_FORMAT = "session:join:session-id:%s";

  private String getJoinKey(String sessionId) {
    return String.format(SESSION_JOIN_FORMAT, sessionId);
  }
}
