package com.edumax.molip.domain.quiz.handler;

import com.edumax.molip.domain.quiz.dto.SttSaveEvent;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveSttText {
  private final RedisTemplate<String, String> redisTemplate;
  String KEY_FORMAT = "quiz:stt:room-id:%s";
  private static final Duration ttl = Duration.ofDays(1);

  @Async
  @EventListener
  public void handleSttResult(SttSaveEvent event) {
    String key = generateKey(event.roomId());
    redisTemplate.opsForZSet().add(key, event.timeline() + ":" + event.text(), event.timeline());
    redisTemplate.expire(key, ttl);
  }

  String generateKey(String roomId) {
    return KEY_FORMAT.formatted(roomId);
  }
}
