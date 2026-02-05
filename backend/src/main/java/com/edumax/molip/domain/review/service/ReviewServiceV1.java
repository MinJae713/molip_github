// package com.edumax.molip.domain.review.service;
//
// import com.edumax.molip.domain.course.entity.Course;
// import com.edumax.molip.domain.course.entity.CourseLearnerAnalysis;
// import com.edumax.molip.domain.course.repository.CourseLearnerAnalysisRepository;
// import com.edumax.molip.domain.course.repository.CourseRepository;
// import com.edumax.molip.domain.review.entity.EmotionGraph;
// import com.edumax.molip.domain.review.entity.LearnerAnalysis;
// import com.edumax.molip.domain.review.repository.EmotionGraphRepository;
// import com.edumax.molip.domain.review.repository.LearnerAnalysisRepository;
// import com.edumax.molip.domain.review.repository.ScriptRepository;
// import com.edumax.molip.domain.session.dto.request.EndAnalysisRequest;
// import com.edumax.molip.domain.session.dto.request.PauseElement;
// import com.edumax.molip.domain.session.dto.response.AnalysisSignalMessage;
// import com.edumax.molip.domain.session.entity.CourseLearnerSession;
// import com.edumax.molip.domain.session.entity.Session;
// import com.edumax.molip.domain.session.handler.AnalysisManager;
// import com.edumax.molip.domain.session.handler.AnalysisManagerRegistry;
// import com.edumax.molip.domain.session.handler.EmotionSaveListener;
// import com.edumax.molip.domain.session.repository.CourseLearnerSessionRepository;
// import com.edumax.molip.domain.session.repository.SessionRepository;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.stream.Collectors;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.RequiredArgsConstructor;
// import lombok.Setter;
// import org.springframework.data.redis.core.RedisCallback;
// import org.springframework.data.redis.core.StringRedisTemplate;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
//
// @Service
// @RequiredArgsConstructor
// public class ReviewServiceV1 {
//  private final SimpMessagingTemplate messagingTemplate;
//  private final StringRedisTemplate redisTemplate;
//
//  private final AnalysisManagerRegistry analysisManagerRegistry;
//  private final EmotionSaveListener emotionSaveListener;
//
//  private final SessionRepository sessionRepository;
//  private final ScriptRepository scriptRepository;
//  private final LearnerAnalysisRepository learnerAnalysisRepository;
//  private final CourseLearnerSessionRepository courseLearnerSessionRepository;
//  private final EmotionGraphRepository emotionGraphRepository;
//  private final CourseLearnerAnalysisRepository courseLearnerAnalysisRepository;
//  private final CourseRepository courseRepository;
//  private final StorageService storageService;
//
//  public void startAnalysis(String sessionId) {
//    emotionSaveListener.startLecture(sessionId);
//    AnalysisManager analysisManager = analysisManagerRegistry.getManager(sessionId);
//    analysisManager.startScheduling(0, sessionId);
//
//    // 모든 참여자에게 분석 시작 시간 보내기
//    String joinKey = getJoinKey(sessionId);
//    Set<String> memberList = redisTemplate.opsForSet().members(joinKey);
//    List<String> memberIdList = new ArrayList<>();
//    if (memberList != null) {
//      for (String member : memberList) {
//        String[] parts = member.split("\\|", 2);
//        if (parts.length == 2) {
//          memberIdList.add(parts[0]);
//        }
//      }
//    }
//    sendAnalysisMessage(memberIdList, sessionId);
//  }
//
//  private void sendAnalysisMessage(List<String> memberIdList, String sessionId) {
//    AnalysisSignalMessage analysisSignalMessage =
//        new AnalysisSignalMessage(LocalDateTime.now().toString());
//    for (String memberId : memberIdList) {
//      messagingTemplate.convertAndSend(
//          "/topic/analysis/" + sessionId + "/" + memberId, analysisSignalMessage);
//    }
//    messagingTemplate.convertAndSend("/topic/analysis/" + sessionId, analysisSignalMessage);
//  }
//
//  private int getRestTime(List<PauseElement> pauseList) {
//    int restTime = 0;
//    for (PauseElement pause : pauseList) {
//      restTime += pause.endTime() - pause.startTime();
//    }
//    return restTime;
//  }
//
//  @Transactional
//  public void endAnalysis(String sessionId, EndAnalysisRequest request) {
//    emotionSaveListener.stopLecture(sessionId); // redis 전송 스케줄러 정지
//    analysisManagerRegistry.removeManager(sessionId);
//
//    Long sessionIdLong = Long.parseLong(sessionId);
//    String joinKey = getJoinKey(sessionId);
//
//    List<Long> memberIdList =
//        redisTemplate.opsForSet().members(joinKey).stream()
//            .map(entry -> Long.parseLong(entry.split("\\|", 2)[0]))
//            .filter(memberId -> !request.getTeacherId().equals(memberId))
//            .toList();
//
//    boolean studentZero = false;
//    Long studentSize = redisTemplate.opsForSet().size(joinKey) - 1; // 강사 제외
//    if (studentSize <= 0) {
//      studentZero = true;
//      studentSize = 1L;
//    }
//
//    boolean totalTimelineZero = false;
//    int totalTimeline = request.getLastTimeline() - getRestTime(request.getPauseElementList());
//    if (totalTimeline <= 0) {
//      totalTimelineZero = true;
//      totalTimeline = 1;
//    }
//
//    Session session =
//        sessionRepository
//            .findById(sessionIdLong)
//            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다. ID: " + sessionId));
//
//    Course course = session.getCourse();
//    int attendance = course.getAttendance();
//    int endSessionCnt = course.getEndSessionCount() + 1;
//
//    // 참여도, 몰입도 관련 처리
//    // TODO: sessionId와 memberId로 사용자가 세션 참가자인지 확인해야함
//
//    List<Object> redisConResult =
//        redisTemplate.executePipelined(
//            (RedisCallback<?>)
//                connection -> {
//                  for (Long memberId : memberIdList) {
//                    String concentrateKey =
//                        "review:concentrate:session-id:%s:member-id:%d"
//                            .formatted(sessionId, memberId);
//                    // byte array로 직접 명령어 전달 (Spring Data Redis의 방식)
//                    connection.listCommands().lRange(concentrateKey.getBytes(), 0, -1);
//                    connection.keyCommands().del(concentrateKey.getBytes());
//                  }
//                  return null;
//                });
//
//    Map<Long, Long> attendanceCntList =
//        courseLearnerSessionRepository.findAllBySessionSessionId(sessionIdLong).stream()
//            .filter(CourseLearnerSession::getIsAttendance) // 출석 인정된 것만
//            .collect(
//                Collectors.groupingBy(CourseLearnerSession::getMemberId, Collectors.counting()));
//
//    List<CourseLearnerSession> sessionResultsToSave = new ArrayList<>();
//
//    // 세션 + 맴버별 분석 저장 공간 업데이트
//    List<CourseLearnerAnalysis> existingAnalyses =
//        courseLearnerAnalysisRepository.findAllByCourseAndMemberIdList(course, memberIdList);
//
//    Map<Long, CourseLearnerAnalysis> analysisMap =
//        existingAnalyses.stream()
//            .collect(Collectors.toMap(CourseLearnerAnalysis::getMemberId, ele -> ele));
//
//    List<CourseLearnerAnalysis> courseResultsToSave = new ArrayList<>();
//
//    float totalSumQuizCorrenct = 0f;
//    float totalSumConcentrate = 0f;
//    float totalSumAttendance = 0f;
//    List<ConcentrateElement> conList = new ArrayList<>();
//
//    for (int i = 0; i < memberIdList.size(); i++) {
//      Long memberId = memberIdList.get(i);
//      List<String> rawConList = (List<String>) redisConResult.get(i * 2);
//
//      if (rawConList == null || rawConList.isEmpty()) {
//        continue;
//      }
//
//      rawConList =
//          rawConList.stream()
//              .sorted(Comparator.comparingInt(s -> Integer.parseInt(s.split("\\|")[0])))
//              .toList();
//
//      conList =
//          rawConList.stream()
//              .map(
//                  s -> {
//                    int timeline = Integer.parseInt(s.split("\\|")[0]);
//                    String[] parts = s.split("\\|")[1].split(",");
//                    boolean empty = Boolean.parseBoolean(parts[0]);
//                    float concentrateValue = Float.parseFloat(parts[1]);
//                    boolean fever = Boolean.parseBoolean(parts[2]);
//                    return new ConcentrateElement(timeline, empty, concentrateValue, fever);
//                  })
//              .toList();
//
//      int attCnt = 0;
//      float conSum = 0f;
//      for (ConcentrateElement ele : conList) {
//        attCnt += (ele.empty() ? 0 : 1);
//        conSum += ele.concentrateValue();
//      }
//
//      boolean isAttendance = false;
//      // 출석 인정
//      if (attCnt * 100 / totalTimeline >= attendance) {
//        isAttendance = true;
//      }
//
//      float avgCon = conSum / totalTimeline;
//      totalSumConcentrate += avgCon;
//
//      CourseLearnerSession courseLearnerSession =
//          CourseLearnerSession.builder()
//              .exitTicket(null) // TODO 퇴실 퀴즈 점수
//              .isAttendance(isAttendance)
//              .attendanceTime(attCnt)
//              .avgConcentrate(avgCon)
//              .courseLearner(null) // TODO CourseLearner 설정
//              .session(session)
//              .memberId(memberId)
//              .build();
//      sessionResultsToSave.add(courseLearnerSession);
//
//      CourseLearnerAnalysis analysis =
//          analysisMap.getOrDefault(
//              memberId,
//              CourseLearnerAnalysis.builder()
//                  .course(course)
//                  .memberId(memberId)
//                  .sumConcentrate(0.0f)
//                  .sumAttendance(0)
//                  .sumQuizCorrect(0.0f)
//                  .build());
//
//      // TODO 퀴즈 정답률 계산 (지금은 0)
//      analysis.updateAnalysis(avgCon, isAttendance, 0.0f);
//      courseResultsToSave.add(analysis);
//
//      long totalAttendanceCount =
//          attendanceCntList.getOrDefault(memberId, 0L) + (isAttendance ? 1 : 0);
//      float avgAttendance = (float) totalAttendanceCount / endSessionCnt * 100;
//
//      totalSumAttendance += avgAttendance;
//    }
//
//    courseLearnerSessionRepository.saveAll(sessionResultsToSave);
//
//    // LearnerAnalysis 삽입
//    float totalAvgAttendance = totalSumAttendance / studentSize;
//    float totalAvgConcentrate = totalSumConcentrate / studentSize;
//    // TODO 퀴즈 정답률 계산 (지금은 0)
//    float totalAvgQuizCorrect = totalSumQuizCorrenct / studentSize;
//
//    LearnerAnalysis learnerAnalysis = session.getLearnerAnalysis();
//
//    if (learnerAnalysis != null) {
//      // 2. 이미 존재한다면 '기존 객체'의 필드만 교체 (Dirty Checking)
//      learnerAnalysis.setTotalLearner(studentZero ? 0 : studentSize.intValue());
//      learnerAnalysis.setTotalAnalysisTime(totalTimelineZero ? 0 : totalTimeline);
//      learnerAnalysis.setAvgAttendance(totalAvgAttendance);
//      learnerAnalysis.setAvgConcentrate(totalAvgConcentrate);
//      learnerAnalysis.setAvgQuizCorrect(totalAvgQuizCorrect);
//
//      // [중요] 여기서 save()를 절대 호출하지 마세요.
//      // 이미 session에 묶여있으므로 트랜잭션 종료 시 자동 반영됩니다.
//    } else {
//      // 3. 진짜로 없을 때만 빌더로 생성
//      learnerAnalysis =
//          LearnerAnalysis.builder()
//              .session(session)
//              .totalLearner(studentZero ? 0 : studentSize.intValue())
//              .totalAnalysisTime(totalTimelineZero ? 0 : totalTimeline)
//              .avgAttendance(totalAvgAttendance)
//              .avgConcentrate(totalAvgConcentrate)
//              .avgQuizCorrect(totalAvgQuizCorrect)
//              .build();
//
//      // 신규 생성시에만 save 호출
//      learnerAnalysis = learnerAnalysisRepository.save(learnerAnalysis);
//    }
//
//    // 감정 관련 처리
//    List<Object> redisEmoResult =
//        redisTemplate.executePipelined(
//            (RedisCallback<?>)
//                connection -> {
//                  for (Long memberId : memberIdList) {
//                    String emotionKey =
//                        "review:emotion:session-id:%s:member-id:%d".formatted(sessionId,
// memberId);
//                    // byte array로 직접 명령어 전달 (Spring Data Redis의 방식)
//                    connection.listCommands().lRange(emotionKey.getBytes(), 0, -1);
//                    connection.keyCommands().del(emotionKey.getBytes());
//                  }
//                  return null;
//                });
//
//    List<EmotionGraph> emotionGraphsToSave = new ArrayList<>();
//    float[] totalSumEmotion = new float[7];
//    List<EmotionElement> emoList = new ArrayList<>();
//
//    for (int i = 0; i < memberIdList.size(); i++) {
//      Long memberId = memberIdList.get(i);
//      List<String> rawEmoList = (List<String>) redisEmoResult.get(i * 2);
//
//      if (rawEmoList == null || rawEmoList.isEmpty()) {
//        continue;
//      }
//
//      rawEmoList =
//          rawEmoList.stream()
//              .sorted(Comparator.comparingInt(s -> Integer.parseInt(s.split("\\|")[0])))
//              .toList();
//
//      emoList =
//          rawEmoList.stream()
//              .map(
//                  s -> {
//                    int timeline = Integer.parseInt(s.split("\\|")[0]);
//                    String[] parts = s.split("\\|")[1].split(",");
//                    EmotionElement result = new EmotionElement(timeline, new float[7]);
//                    for (String part : parts) {
//                      String name = part.split(":", 2)[0];
//                      float value = Float.parseFloat(part.split(":", 2)[1]);
//                      result.emotionValueList[AnalysisManager.getEmotionIndex(name)] = value;
//                    }
//                    return result;
//                  })
//              .toList();
//
//      float[] emoSum = new float[7];
//      for (EmotionElement ele : emoList) {
//        for (int e = 0; e < 7; e++) {
//          emoSum[e] += ele.emotionValueList[e];
//        }
//      }
//
//      for (int e = 0; e < 7; e++) {
//        emoSum[e] = emoSum[e] / totalTimeline;
//        totalSumEmotion[e] += emoSum[e];
//        EmotionGraph emotionGraph =
//            EmotionGraph.builder()
//                .name(AnalysisManager.toEmotion(e))
//                .value(emoSum[e])
//                .learnerAnalysis(learnerAnalysis)
//                .memberId(memberId)
//                .build();
//
//        emotionGraphsToSave.add(emotionGraph);
//      }
//    }
//
//    for (int e = 0; e < 7; e++) { // teacherId로 조회하는 육각형은 학생평균값
//      EmotionGraph avgEmotionGraph =
//          EmotionGraph.builder()
//              .name(AnalysisManager.toEmotion(e))
//              .value(totalSumEmotion[e] / studentSize)
//              .learnerAnalysis(learnerAnalysis)
//              .memberId(request.getTeacherId())
//              .build();
//      emotionGraphsToSave.add(avgEmotionGraph);
//    }
//    // 합친 리스트 얻기
//
//    // 감정 칠각형 추가
//    emotionGraphRepository.saveAll(emotionGraphsToSave);
//
//    // 강의 맴버 분석 테이블 업데이트
//    courseLearnerAnalysisRepository.saveAll(courseResultsToSave);
//    courseLearnerAnalysisRepository.flush();
//
//    // 강의 테이블 업데이트
//    Long courseStudentCnt =
//        courseLearnerAnalysisRepository.countByCourseCourseId(course.getCourseId());
//    // course.getCourseLearnerCount()로 변경 가능
//    if (courseStudentCnt == 0) {
//      courseStudentCnt = 1L;
//    }
//
//    List<Object[]> resultList =
//        courseLearnerAnalysisRepository.findSumByCourseId(course.getCourseId());
//    float totalSumAtt = 0f;
//    float totalSumCon = 0f;
//    float totalSumQuiz = 0f;
//
//    if (resultList != null && !resultList.isEmpty()) {
//      Object[] row = resultList.get(0); // 실제 데이터가 담긴 첫 번째 행(배열)을 추출
//
//      totalSumAtt = (row[0] != null) ? ((Number) row[0]).floatValue() : 0f;
//      totalSumCon = (row[1] != null) ? ((Number) row[1]).floatValue() : 0f;
//      totalSumQuiz = (row[2] != null) ? ((Number) row[2]).floatValue() : 0f;
//    }
//
//    float denominator = (float) endSessionCnt * courseStudentCnt;
//
//    course.setAvgAttendance(totalSumAtt / denominator * 100f);
//    course.setAvgConcentrate(totalSumCon / denominator);
//    course.setAvgQuizCorrect(totalSumQuiz / denominator);
//
//    course.setEndSessionCount(endSessionCnt);
//    courseRepository.save(course);
//
//    // TODO stt 데이터 script 테이블로 이동
//  }
//
//  @Getter
//  @Setter
//  @NoArgsConstructor
//  @AllArgsConstructor
//  private class MergedElement {
//    int timeline;
//    boolean empty;
//    float concentrateValue;
//    boolean fever;
//    int emotionIndex; // 1등 감정 인덱스
//    String emotionName; // 1등 감정 이름
//    float emotionValue; // 1등 감정값
//  }
//
//  @Getter
//  @Setter
//  @NoArgsConstructor
//  @AllArgsConstructor
//  private class EmotionElement {
//    int timeline;
//    float[] emotionValueList;
//  }
//
//  record ConcentrateElement(int timeline, boolean empty, float concentrateValue, boolean fever) {}
//
//  String QUIZ_STT_FORMAT = "quiz:stt:room-id:%s";
//
//  String generateKey(String roomId) {
//    return QUIZ_STT_FORMAT.formatted(roomId);
//  }
//
//  String SESSION_JOIN_FORMAT = "session:join:session-id:%s";
//
//  private String getJoinKey(String sessionId) {
//    return String.format(SESSION_JOIN_FORMAT, sessionId);
//  }
// }
