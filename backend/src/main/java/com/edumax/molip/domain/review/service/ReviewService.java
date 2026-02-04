package com.edumax.molip.domain.review.service;

import com.edumax.molip.domain.chatting.service.ChattingService;
import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseLearnerAnalysis;
import com.edumax.molip.domain.course.repository.CourseLearnerAnalysisRepository;
import com.edumax.molip.domain.course.repository.CourseRepository;
import com.edumax.molip.domain.review.entity.EmotionGraph;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
  private final ChattingService chattingService;

  private int trainerFeverRate = 50; // 학생중 50% 이상이 열정이면 강사도 타임라인도 열정
  private int trainerEmptyRate = 50; // 학생중 50% 이상이 부재중이면 강사 타임라인도 부재중

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
    log.info("[분석 종료 시작] SessionId: {}, TrainerId: {}", sessionId, trainerId);

    emotionSaveListener.stopLecture(sessionId);
    analysisManagerRegistry.removeManager(sessionId);

    Long sessionIdLong = Long.parseLong(sessionId);
    String joinKey = getJoinKey(sessionId);

    List<Long> memberIdList =
        redisTemplate.opsForSet().members(joinKey).stream()
            .map(entry -> Long.parseLong(entry.split("\\|", 2)[0]))
            .filter(memberId -> !trainerId.equals(memberId))
            .toList();

    log.info("[분석 대상] 학생 수: {}명, 대상 ID: {}", memberIdList.size(), memberIdList);

    boolean studentZero = false;
    Long studentSizeTmp = redisTemplate.opsForSet().size(joinKey) - 1;
    if (studentSizeTmp <= 0) {
      log.warn("[주의] 세션 참여 학생이 0명입니다.");
      studentZero = true;
      studentSizeTmp = 1L;
    }
    final Long studentSize = studentSizeTmp;

    int totalTimeline = request.getLastTimeline() - getRestTime(request.getPauseElementList());
    if (totalTimeline <= 0) {
      log.warn("[주의] 유효 분석 시간이 0 이하입니다. (입력: {})", totalTimeline);
      totalTimeline = 1;
    }

    Session session =
        sessionRepository
            .findById(sessionIdLong)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));

    Course course = session.getCourse();
    int attendance = course.getAttendance();
    int endSessionCnt = course.getEndSessionCount() + 1;

    // stt 스크립트 MySQL 저장
    storageService.saveScriptToMysql(sessionIdLong);

    // 1. 몰입도/감정 데이터 Pipeline 실행
    log.info("[Redis] 데이터 추출 파이프라인 가동...");
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

    List<CourseLearnerSession> sessionResultsToSave = new ArrayList<>();
    List<CourseLearnerAnalysis> existingAnalyses =
        courseLearnerAnalysisRepository.findAllByCourseAndMemberIdList(course, memberIdList);
    Map<Long, CourseLearnerAnalysis> analysisMap =
        existingAnalyses.stream()
            .collect(Collectors.toMap(CourseLearnerAnalysis::getMemberId, ele -> ele));

    List<CourseLearnerAnalysis> courseResultsToSave = new ArrayList<>();
    List<EmotionGraph> emotionGraphsToSave = new ArrayList<>();
    //    float totalSumQuizCorrenct = 0f;
    float totalSumConcentrate = 0f;
    float totalSumAttendance = 0f;
    float[] totalSumEmotion = new float[7];

    Map<Integer, float[]> trainerAggregationMap = new TreeMap<>();

    log.info("[분석 시작] 멤버별 데이터 집계 및 MongoDB 저장 프로세스 실행...");
    for (int i = 0; i < memberIdList.size(); i++) {
      Long memberId = memberIdList.get(i);
      List<String> rawConList = (List<String>) redisConResult.get(i * 2);
      List<String> rawEmoList = (List<String>) redisEmoResult.get(i * 2);

      if (rawConList == null || rawConList.isEmpty()) {
        continue;
      }

      List<ConcentrateElement> conList =
          rawConList.stream()
              .map(
                  s -> {
                    int tl = Integer.parseInt(s.split("\\|")[0]);
                    String[] p = s.split("\\|")[1].split(",");
                    return new ConcentrateElement(
                        tl,
                        Boolean.parseBoolean(p[0]),
                        Float.parseFloat(p[1]),
                        Boolean.parseBoolean(p[2]));
                  })
              .sorted(Comparator.comparingInt(ConcentrateElement::timeline))
              .toList();

      List<EmotionElement> emoList =
          (rawEmoList == null)
              ? new ArrayList<>()
              : rawEmoList.stream()
                  .map(
                      s -> {
                        int tl = Integer.parseInt(s.split("\\|")[0]);
                        String[] p = s.split("\\|")[1].split(",");
                        EmotionElement res = new EmotionElement(tl, new float[7]);
                        for (String part : p) {
                          String name = part.split(":", 2)[0];
                          res.getEmotionValueList()[AnalysisManager.getEmotionIndex(name)] =
                              Float.parseFloat(part.split(":", 2)[1]);
                        }
                        return res;
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
                    float[] agg =
                        trainerAggregationMap.computeIfAbsent(con.timeline(), k -> new float[10]);
                    agg[0] += con.concentrateValue();
                    agg[1] += (con.fever() ? 1 : 0);
                    agg[2] += (con.empty() ? 1 : 0);
                    for (int e = 0; e < 7; e++) {
                      agg[3 + e] += emo.getEmotionValueList()[e];
                    }

                    int maxIdx = 0;
                    float maxVal = emo.getEmotionValueList()[0];
                    for (int j = 1; j < 7; j++) {
                      if (emo.getEmotionValueList()[j] > maxVal) {
                        maxVal = emo.getEmotionValueList()[j];
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
              //              .filter(Objects::nonNull)
              .toList();

      log.info(" - [학생 저장] ID: {}, 타임라인 데이터 수: {}", memberId, mergedList.size());
      storageService.saveTimelineAnalysis(sessionIdLong, memberId, mergedList);

      int attCnt = 0;
      float conSum = 0f;
      for (ConcentrateElement ele : conList) {
        attCnt += (ele.empty() ? 0 : 1);
        conSum += ele.concentrateValue();
      }

      boolean isAttendance = (attCnt * 100 / totalTimeline) >= attendance;
      float avgCon = conSum / totalTimeline;
      totalSumConcentrate += avgCon;

      if (isAttendance) {
        totalSumAttendance += 1.0f;
      }

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

      float[] emoSum = new float[7];
      for (EmotionElement ele : emoList) {
        for (int e = 0; e < 7; e++) {
          emoSum[e] += ele.getEmotionValueList()[e];
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

    // --- 강사 데이터 생성 및 로그 ---
    List<TimelineElement> trainerMergedList =
        trainerAggregationMap.entrySet().stream()
            .map(
                entry -> {
                  int tl = entry.getKey();
                  float[] agg = entry.getValue();
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
                      agg[2] >= (studentSize * trainerEmptyRate / 100.0),
                      agg[0] / studentSize,
                      agg[1] > (studentSize * trainerFeverRate / 100.0),
                      maxIdx,
                      AnalysisManager.toEmotion(maxIdx));
                })
            .toList();

    log.info("[강사 데이터 저장] ID: {}, 종합 타임라인 수: {}", trainerId, trainerMergedList.size());
    storageService.saveTimelineAnalysis(sessionIdLong, trainerId, trainerMergedList);

    // --- 집계 데이터 요약 로그 ---
    float finalAvgAtt = totalSumAttendance * 100f / studentSize;
    float finalAvgCon = totalSumConcentrate * 100f / studentSize;
    log.info("[집계 완료] 학생평균 출석률: {}%, 평균 몰입도: {}", finalAvgAtt, finalAvgCon);

    LearnerAnalysis learnerAnalysis =
        learnerAnalysisRepository.save(
            LearnerAnalysis.builder()
                .session(session)
                .totalLearner(studentZero ? 0 : studentSize.intValue())
                .totalAnalysisTime(totalTimeline)
                .avgAttendance(finalAvgAtt)
                .avgConcentrate(finalAvgCon)
                .avgQuizCorrect(0f)
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

    log.info("[MySQL 저장] 감정 그래프({})개 저장 시작...", finalEmotions.size());
    emotionGraphRepository.saveAll(finalEmotions);
    log.info("[MySQL 저장] 세션이수정보({})개 저장 시작...", sessionResultsToSave.size());
    courseLearnerSessionRepository.saveAll(sessionResultsToSave);
    log.info("[MySQL 저장] 강의수강생 분석정보({})개 저장 시작...", sessionResultsToSave.size());
    courseLearnerAnalysisRepository.saveAll(courseResultsToSave);
    courseLearnerAnalysisRepository.flush();

    log.info("[코스 통계 업데이트] 과목명: {}, 진행 세션 수: {}", course.getCourseId(), endSessionCnt);
    updateCourseStatistics(course, endSessionCnt);
    log.info("[분석 종료 완료] SessionId: {} 프로세스가 정상 종료되었습니다.", sessionId);
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

  String SESSION_JOIN_FORMAT = "session:join:session-id:%s";

  private String getJoinKey(String sessionId) {
    return String.format(SESSION_JOIN_FORMAT, sessionId);
  }
}
