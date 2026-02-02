package com.edumax.molip.domain.session.handler;

import com.edumax.molip.domain.session.dto.ConcentrateSaveEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConcentrateSaveListener {
  private final StringRedisTemplate redisTemplate;
  private final ThreadPoolTaskScheduler taskScheduler; // 동적 스케줄링을 위한 주입
  // 세션별로 실행 중인 스케줄러 작업(Future)을 저장
  private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

  // 이벤트를 임시 보관할 큐
  private final BlockingQueue<ConcentrateSaveEvent> eventBuffer = new LinkedBlockingQueue<>();

  private static final String GENERAL_KEY_FORMAT = "review:concentrate:session-id:%s";
  private static final String PERSONAL_KEY_FORMAT = "review:concentrate:session-id:%s:member-id:%d";
  private static final Duration TTL = Duration.ofDays(1);
  private static final int BATCH_SIZE = 100;

  /** 강의 시작 시 호출 (Controller나 Service에서 호출) */
  public void startLecture(String sessionId) {
    // 이미 실행 중인 경우 중복 생성 방지
    if (scheduledTasks.containsKey(sessionId)) {
      return;
    }

    // 10초마다 flushBatch를 실행하도록 등록
    ScheduledFuture<?> future =
        taskScheduler.scheduleWithFixedDelay(this::flushBatch, Duration.ofSeconds(10));
    scheduledTasks.put(sessionId, future);
  }

  /** 강의 종료 시 호출 */
  public void stopLecture(String sessionId) {
    ScheduledFuture<?> future = scheduledTasks.remove(sessionId);
    if (future != null) {
      future.cancel(false); // 스케줄러 정지
      flushBatch(); // 종료 전 남은 데이터 최종 처리
    }
  }

  @Async("concentrateSaveExecutor")
  @EventListener
  public void handleConcentrateResult(ConcentrateSaveEvent event) {
    eventBuffer.offer(event);
    if (eventBuffer.size() >= BATCH_SIZE) {
      flushBatch();
    }
  }

  // synchronized를 통해 여러 트리거가 동시에 flush하는 것을 방지
  public synchronized void flushBatch() {
    if (eventBuffer.isEmpty()) {
      return;
    }

    List<ConcentrateSaveEvent> batch = new ArrayList<>();
    eventBuffer.drainTo(batch);

    if (batch.isEmpty()) {
      return;
    }
    Set<String> processedKeys = new HashSet<>();

    redisTemplate.executePipelined(
        (RedisCallback<?>)
            connection -> {
              for (ConcentrateSaveEvent item : batch) {
                String genKeyStr = generateGeneralKey(item.sessionId());
                String perKeyStr = generatePersonalKey(item.sessionId(), item.memberId());

                byte[] genKey = redisTemplate.getStringSerializer().serialize(genKeyStr);
                byte[] perKey = redisTemplate.getStringSerializer().serialize(perKeyStr);

                // score(timeline)를 제외하고 데이터 포맷 구성
                // 필요하다면 timeline을 데이터 앞에 포함시켜 나중에 정렬 순서를 알 수 있게 유지
                String perValueStr =
                    String.format(
                        "%d|%b,%.3f,%b",
                        item.timeline(), // 타임라인 정보는 값 내부에 포함
                        item.emptyState(),
                        item.concentrateValue(),
                        item.feverTime());

                String genValueStr =
                    String.format("%d|%.3f", item.timeline(), item.concentrateValue());

                byte[] perValue = redisTemplate.getStringSerializer().serialize(perValueStr);
                byte[] genValue = redisTemplate.getStringSerializer().serialize(genValueStr);

                // [수정] ZAdd 대신 List의 rPush 사용 (오른쪽으로 추가하여 시간순 유지)
                connection.listCommands().rPush(genKey, genValue);
                connection.listCommands().rPush(perKey, perValue);

                // Expire 설정 유지
                if (processedKeys.add(genKeyStr)) {
                  connection.keyCommands().expire(genKey, TTL.getSeconds());
                }
                if (processedKeys.add(perKeyStr)) {
                  connection.keyCommands().expire(perKey, TTL.getSeconds());
                }
              }
              return null;
            });
  }

  private String generateGeneralKey(String sessionId) {
    return GENERAL_KEY_FORMAT.formatted(sessionId);
  }

  private String generatePersonalKey(String sessionId, Long memberId) {
    return PERSONAL_KEY_FORMAT.formatted(sessionId, memberId);
  }
}
