package com.edumax.molip.domain.review.service;

import com.edumax.molip.domain.review.repository.ScriptInsertRepository;
import com.edumax.molip.domain.review.repository.ScriptRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
  private final StringRedisTemplate redisTemplate;
  private final ScriptInsertRepository scriptInsertRepository;
  private final ScriptRepository scriptRepository;

  String KEY_FORMAT = "quiz:stt:room-id:%s";

  public void saveSttText(String roomId, Long sessionId) {
    List<Long> timelineList = new ArrayList<>();
    List<String> textList = new ArrayList<>();

    String key = generateKey(roomId);
    redisTemplate.opsForZSet().rangeWithScores(key, 0, -1).stream()
        .forEach(
            tuple -> {
              timelineList.add((long) tuple.getScore().doubleValue());
              textList.add(tuple.getValue().split(":")[1]);
            });

    scriptInsertRepository.bulkInsertScripts(timelineList, textList, sessionId);
  }

  String generateKey(String roomId) {
    return KEY_FORMAT.formatted(roomId);
  }
}
