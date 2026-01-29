package com.edumax.molip.domain.quiz.handler;

import com.edumax.molip.domain.quiz.dto.SttSaveEvent;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SttSaveListener {

  // StringRedisTemplate으로 변경
  private final StringRedisTemplate redisTemplate;

  // 상수는 static final로 관리하는 것이 메모리 효율상 좋습니다.
  private static final String KEY_FORMAT = "quiz:stt:session-id:%s";
  private static final Duration TTL = Duration.ofDays(1);

  @Async("sttSaveExecutor")
  @EventListener
  public void handleSttResult(SttSaveEvent event) {
    String key = generateKey(event.sessionId());

    String value = event.timeline() + "|" + event.text();
    redisTemplate.opsForZSet().add(key, value, event.timeline());
    redisTemplate.expire(key, TTL);
  }

  private String generateKey(String sessionId) {
    return String.format(KEY_FORMAT, sessionId);
  }
}
