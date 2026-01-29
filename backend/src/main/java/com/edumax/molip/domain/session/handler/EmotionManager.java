package com.edumax.molip.domain.session.handler;

import com.edumax.molip.domain.session.dto.response.TeacherConcentrateMessage;
import com.edumax.molip.domain.session.dto.response.TeacherEmotionMessage;
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
public class EmotionManager { // 세션마다 존재
  // 1. 데이터 구조 정의: timeline별로 7개의 감정 수치를 담는 배열
  private final Map<Integer, List<float[]>> emotionBuffer = new ConcurrentHashMap<>();
  private final Map<Integer, List<Float>> concentrationBuffer = new ConcurrentHashMap<>();
  private final Map<Integer, Deque<Integer>> feverTimeBuffer =
      new ConcurrentHashMap<>(); // memberId -> fever타임라인
  private final Map<Integer, Integer> lastScorebuffer =
      new ConcurrentHashMap<>(); // memberId -> lastScore
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final SimpMessagingTemplate messagingTemplate; // 웹소켓 전송을 위한 템플릿
  private ScheduledFuture<?> scheduledTask; // 실행 중인 작업을 담는 변수

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

  public void insertConcentrate(int timeline, Float concentrateValue) {
    log.info("집중도 삽입 시도 - 타임라인: {}", timeline);
    List<Float> studentConcentrateList =
        concentrationBuffer.computeIfAbsent(timeline, k -> new ArrayList<>());

    synchronized (studentConcentrateList) {
      studentConcentrateList.add(concentrateValue);
    }
    log.debug("데이터 삽입 완료 - 현재 해당 타임라인 데이터 수: {}", studentConcentrateList.size());
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
                List<float[]> emotionValues = emotionBuffer.remove(timeline);
                List<Float> concentrateValues = concentrationBuffer.remove(timeline);

                int emotionCount = emotionValues.size(); // 참여 학생 수
                int concentrateCount = concentrateValues.size(); // 참여 학생 수

                // 매 초마다 스케줄러가 돌고 있다는 로그
                log.debug("[{}] 스케줄러 작동 중 - 타임라인: {}", sessionId, timeline);

                if (emotionValues != null && !emotionValues.isEmpty()) {
                  float[] avgList = new float[7];
                  Float avgConcentrate = 0f;

                  synchronized (emotionValues) {
                    for (float[] studentData : emotionValues) {
                      for (int i = 0; i < 7; i++) {
                        avgList[i] += studentData[i];
                      }
                    }
                  }

                  synchronized (concentrateValues) {
                    for (Float studentData : concentrateValues) {
                      for (int i = 0; i < 7; i++) {
                        avgConcentrate += studentData;
                      }
                    }
                  }

                  for (int i = 0; i < avgList.length; i++) {
                    avgList[i] = avgList[i] / emotionCount;
                  }
                  avgConcentrate /= concentrateCount;

                  log.info("[{}] 감정 전송 - 타임라인: {}, 참여 학생 수: {}", sessionId, timeline, emotionCount);
                  sendEmotion(timeline, avgList, sessionId);
                  log.info(
                      "[{}] 집중도 전송 - 타임라인: {}, 참여 학생 수: {}", sessionId, timeline, concentrateCount);
                  sendConcentrate(timeline, avgConcentrate, sessionId);
                } else {
                  // 데이터가 없어서 전송을 스킵하는 경우
                  log.trace("[{}] 타임라인 {}에 전송할 데이터가 없습니다.", sessionId, timeline);
                }

                currentTimeline[0]++;
              } catch (Exception e) {
                // 이 로그가 찍힌다면 스케줄러 내부 로직에서 에러가 난 것입니다.
                log.error("[{}] 스케줄러 실행 중 치명적 오류 발생!", sessionId, e);
              }
            },
            10,
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
  }

  private void sendEmotion(int timeline, float[] avgList, String sessionId) {
    TeacherEmotionMessage message =
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
            avgList[6]);

    messagingTemplate.convertAndSend("/topic/analysis/" + sessionId, message);
  }

  private void sendConcentrate(int timeline, Float avgList, String sessionId) {
    TeacherConcentrateMessage message = new TeacherConcentrateMessage(timeline, avgList);

    messagingTemplate.convertAndSend("/topic/analysis/" + sessionId, message);
  }
}
