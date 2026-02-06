package com.edumax.molip.domain.session.handler;

import com.edumax.molip.domain.session.dto.response.TeacherAnalysisMessage;
import com.edumax.molip.domain.session.dto.response.TeacherConcentrateMessage;
import com.edumax.molip.domain.session.dto.response.TeacherEmotionMessage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@RequiredArgsConstructor
public class AnalysisManager { // 세션마다 존재
  // 1. 데이터 구조 정의: timeline별로 7개의 감정 수치를 담는 배열
  private final Map<Integer, List<float[]>> emotionBuffer = new ConcurrentHashMap<>();
  private final Map<Integer, List<Float>> concentrationBuffer = new ConcurrentHashMap<>();
  // memberId -> fever타임라인
  private final Map<Long, Deque<Integer>> feverTimeBuffer = new ConcurrentHashMap<>();
  // memberId -> lastScore
  private final Map<Long, Float> lastScoreBuffer = new ConcurrentHashMap<>();
  // empty time -> initialize or not
  private final Map<Long, Deque<Integer>> emptyBuffer = new ConcurrentHashMap<>();
  // 멤버별 마지막 상태 저장
  private final Map<Long, State> lastStateBuffer = new ConcurrentHashMap<>();
  // 상태 변화 전송용 임시 버퍼
  private final Map<Long, StateChange> stateChangeBuffer = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final SimpMessagingTemplate messagingTemplate; // 웹소켓 전송을 위한 템플릿
  private ScheduledFuture<?> scheduledTask; // 실행 중인 작업을 담는 변수

  private static final int EMPTY_LIMIT = 5 + 1; // 180초간 자리비움하면 자리비움 처리
  private static final int FEVER_LIMIT = 3 + 1; // 180초간 풀집중이면 하면 피버타임 처리
  private static final float CONCENTRATE_DEFAULT = 30;

  enum State {
    NORMAL,
    FEVER,
    EMPTY
  }

  public record StateChange(Long memberId, State newState) {}

  // 감정 이름에 따른 배열 인덱스 매핑 (예시
  public static int getEmotionIndex(String emotionName) {
    return switch (emotionName) {
      case "happy" -> 0;
      case "sad" -> 1;
      case "angry" -> 2;
      case "surprise" -> 3;
      case "fear" -> 4;
      case "disgust" -> 5;
      case "neutral" -> 6;
      default -> -1;
    };
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

  public boolean insertFeverState(String sessionId, Long memberId, int timeline) {
    log.info("자리비움 삽입 시도 : [{}] - {} - {}", timeline, sessionId, memberId);
    Long key = memberId;
    Deque<Integer> feverDeque = feverTimeBuffer.computeIfAbsent(key, k -> new ArrayDeque<>());
    State lastState = lastStateBuffer.computeIfAbsent(key, k -> State.NORMAL);

    synchronized (feverDeque) {
      // 1. 사이즈 제한 관리
      while (feverDeque.size() >= FEVER_LIMIT) {
        feverDeque.pollFirst();
      }

      // 2. 새로운 타임라인 추가
      feverDeque.addLast(timeline);

      // 3. 꽉 찼을 때만 로직 검사
      if (feverDeque.size() == FEVER_LIMIT) {
        // 마지막 값과 첫 값의 차이가 (LIMIT - 1)이면 연속된 것으로 간주
        if (feverDeque.getLast() - feverDeque.getFirst() == FEVER_LIMIT - 1) {
          log.info("피버타임 처리됨 : {} - {} - {}", timeline, sessionId, feverDeque.size());
          if (lastState != State.FEVER) {
            stateChangeBuffer.put(memberId, new StateChange(memberId, State.FEVER));
          }

          lastStateBuffer.put(key, State.FEVER);
          return true;
        }
      }

      if (lastState == State.FEVER) {
        if (feverDeque.getLast() - feverDeque.getFirst() != FEVER_LIMIT - 1) {
          stateChangeBuffer.put(memberId, new StateChange(memberId, State.NORMAL));
          lastStateBuffer.put(key, State.NORMAL);
        }
      }
    }

    return false;
  }

  public boolean insertEmpty(String sessionId, Long memberId, int timeline) {
    log.info("자리비움 삽입 시도 : {} - {} - {}", timeline, sessionId, memberId);
    Long key = memberId;
    Deque<Integer> emptyDeque = emptyBuffer.computeIfAbsent(key, k -> new ArrayDeque<>());
    State lastState = lastStateBuffer.computeIfAbsent(key, k -> State.NORMAL);

    synchronized (emptyDeque) {
      // 1. 사이즈 제한 관리
      while (emptyDeque.size() >= EMPTY_LIMIT) {
        emptyDeque.pollFirst();
      }

      // 2. 새로운 타임라인 추가
      emptyDeque.addLast(timeline);

      // 3. 꽉 찼을 때만 로직 검사
      if (emptyDeque.size() == EMPTY_LIMIT) {
        // 마지막 값과 첫 값의 차이가 (LIMIT - 1)이면 연속된 것으로 간주
        if (emptyDeque.getLast() - emptyDeque.getFirst() == EMPTY_LIMIT - 1) {
          log.info("자리비움 처리됨 : {} - {} - {}", timeline, sessionId, emptyDeque.size());
          if (lastState != State.EMPTY) {
            stateChangeBuffer.put(memberId, new StateChange(memberId, State.EMPTY));
          }
          lastStateBuffer.put(key, State.EMPTY);

          return true;
        }
      }

      if (lastState == State.FEVER) {
        if (emptyDeque.getLast() - emptyDeque.getFirst() != FEVER_LIMIT - 1) {
          stateChangeBuffer.put(memberId, new StateChange(memberId, State.NORMAL));
          lastStateBuffer.put(key, State.NORMAL);
        }
      }
    }

    return false;
  }

  /** 학생들이 1초마다 감정 정보를 삽입하는 메서드 */
  public void insertEmotion(int timeline, float[] emotionValueList) {
    log.info("감정 삽입 시도 - 타임라인: {}", timeline);
    List<float[]> studentEmotionList =
        emotionBuffer.computeIfAbsent(timeline, k -> new ArrayList<>());

    synchronized (studentEmotionList) {
      studentEmotionList.add(emotionValueList);
    }
    log.debug("데이터 삽입 완료 - 현재 해당 타임라인 데이터 수: {}", studentEmotionList.size());
  }

  public float insertConcentrate(
      String sessionId, Long memberId, int timeline, boolean isEmpty, float diff) {
    log.info("집중도 삽입 시도 : {} -empty {} -diff {}", timeline, isEmpty, diff);

    Long key = memberId;
    Float lastScore = lastScoreBuffer.getOrDefault(key, CONCENTRATE_DEFAULT);

    if (isEmpty) {
      lastScore = CONCENTRATE_DEFAULT;
    } else {
      lastScore += diff;
      if (lastScore > 100) {
        lastScore = 100f;
      }
      if (lastScore < 0) {
        lastScore = 0f;
      }
    }

    lastScoreBuffer.put(key, lastScore);

    List<Float> studentConcentrateList =
        concentrationBuffer.computeIfAbsent(timeline, k -> new ArrayList<>());

    synchronized (studentConcentrateList) {
      studentConcentrateList.add(lastScore);
    }

    log.debug("데이터 삽입 완료 - 현재 해당 타임라인 데이터 수: {}", studentConcentrateList.size());

    return lastScore;
  }

  /**
   * 스케줄러 시작 트리거
   *
   * @param startTimeline 시작할 타임라인 포인트
   */
  public void startScheduling(int startTimeline, String sessionId) {
    if (scheduledTask != null && !scheduledTask.isCancelled()) {
      log.warn("[{}] 스케줄러가 이미 실행 중입니다.", sessionId);
      return;
    }

    final int[] currentTimeline = {startTimeline};
    log.info("[{}] 스케줄러를 시작합니다. 시작 타임라인: {}", sessionId, startTimeline);

    scheduledTask =
        scheduler.scheduleAtFixedRate(
            () -> {
              try { // 스케줄러 내부의 예외는 반드시 try-catch로 감싸야 중단되지 않습니다.
                int timeline = currentTimeline[0];
                List<float[]> emotionValues = emotionBuffer.get(timeline);

                log.debug("[{}] 스케줄러 작동 중 - 타임라인: {}", sessionId, timeline);

                TeacherAnalysisMessage analysisMessage = new TeacherAnalysisMessage();

                // 2. 데이터가 하나라도 없는 경우에 대한 방어 코드
                if (emotionValues == null || emotionValues.isEmpty()) {
                  analysisMessage.setEmotionMessage(
                      new TeacherEmotionMessage(
                          "emotion",
                          timeline,
                          toEmotion(0),
                          0f,
                          toEmotion(1),
                          0f,
                          toEmotion(2),
                          0f,
                          toEmotion(3),
                          0f,
                          toEmotion(4),
                          0f,
                          toEmotion(5),
                          0f,
                          toEmotion(6),
                          0f));
                  log.trace("[{}] 타임라인 {}에 감정 데이터가 불충분하여 스킵합니다.", sessionId, timeline);
                } else {
                  int emotionCount = emotionValues.size(); // 참여 학생 수
                  float[] avgList = new float[7];

                  synchronized (emotionValues) {
                    for (float[] studentData : emotionValues) {
                      for (int i = 0; i < 7; i++) {
                        avgList[i] += studentData[i];
                      }
                    }
                  }

                  for (int i = 0; i < 7; i++) {
                    avgList[i] /= emotionCount;
                  }

                  log.info("[{}] 감정 전송 - 타임라인: {}, 참여 학생 수: {}", sessionId, timeline, emotionCount);

                  analysisMessage.setEmotionMessage(
                      new TeacherEmotionMessage(
                          "emotion",
                          timeline,
                          toEmotion(0),
                          avgList[0],
                          toEmotion(1),
                          avgList[1],
                          toEmotion(2),
                          avgList[2],
                          toEmotion(3),
                          avgList[3],
                          toEmotion(4),
                          avgList[4],
                          toEmotion(5),
                          avgList[5],
                          toEmotion(6),
                          avgList[6]));
                }

                List<Float> concentrateValues = concentrationBuffer.get(timeline);

                if (concentrateValues == null || concentrateValues.isEmpty()) {
                  analysisMessage.setConcentrateMessage(
                      new TeacherConcentrateMessage("concentrate", timeline, 0f));
                  log.trace("[{}] 타임라인 {}에 몰입도 데이터가 불충분하여 스킵합니다.", sessionId, timeline);
                } else {
                  int concentrateCount = concentrateValues.size(); // 참여 학생 수
                  Float avgConcentrate = 0f;

                  synchronized (concentrateValues) {
                    for (Float studentData : concentrateValues) {
                      avgConcentrate += studentData;
                    }
                  }

                  avgConcentrate /= concentrateCount;

                  log.info(
                      "[{}] 집중도 전송 - 타임라인: {}, 참여 학생 수: {}", sessionId, timeline, concentrateCount);

                  analysisMessage.setConcentrateMessage(
                      new TeacherConcentrateMessage("concentrate", timeline, avgConcentrate));
                }

                if (stateChangeBuffer.isEmpty()) {
                  analysisMessage.setStateChangeList(new ArrayList<>());
                } else {
                  log.info(
                      "[{}] 상태 변화 전송 - 타임라인: {}, 변화 수: {}",
                      sessionId,
                      timeline,
                      stateChangeBuffer.size());
                  List<StateChange> changes = new ArrayList<>(stateChangeBuffer.values());
                  stateChangeBuffer.clear();
                  analysisMessage.setStateChangeList(changes);
                }

                // 5. 웹소켓으로 전송
                messagingTemplate.convertAndSend("/topic/analysis/" + sessionId, analysisMessage);

                emotionBuffer.remove(timeline);
                concentrationBuffer.remove(timeline);
                currentTimeline[0]++;
              } catch (Exception e) {
                // 이 로그가 찍힌다면 스케줄러 내부 로직에서 에러가 난 것입니다.
                log.error("[{}] 스케줄러 실행 중 치명적 오류 발생!", sessionId, e);
              }
            },
            5,
            1,
            TimeUnit.SECONDS);
  }

  /** 강의 종료 시 호출하여 특정 작업만 멈춤 */
  public void endLecture() {
    System.out.println("Shutting down the scheduler...");

    // 1. 새로운 작업을 받지 않고, 기존 작업이 끝나면 종료
    scheduler.shutdown();

    try {
      // 2. 최대 5초간 기다려보고 그래도 안 끝나면 강제 종료
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }

    // 3. 남은 버퍼 정리
    emotionBuffer.clear();
    concentrationBuffer.clear();
    feverTimeBuffer.clear();
    lastScoreBuffer.clear();
    emptyBuffer.clear();
    lastStateBuffer.clear();
    stateChangeBuffer.clear();
  }
}
