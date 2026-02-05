package com.edumax.molip.domain.review.service;

import com.edumax.molip.domain.review.dto.TimelineAnalysis;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
import com.edumax.molip.domain.review.entity.Script;
import com.edumax.molip.domain.review.entity.TimelineElement;
import com.edumax.molip.domain.review.repository.ScriptRepository;
import com.edumax.molip.domain.review.repository.TimelineAnalysisRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
  private final StringRedisTemplate redisTemplate;
  private final TimelineAnalysisRepository timelineAnalysisRepository;
  private final ScriptRepository scriptRepository;

  /** 데이터를 직렬화하여 MongoDB에 저장 */
  public void saveTimelineAnalysis(
      Long sessionId, Long memberId, List<TimelineElement> timelineElementList) {
    try {
      TimelineAnalysis timelineAnalysis =
          TimelineAnalysis.builder()
              .sessionId(sessionId)
              .memberId(memberId)
              .saveTime(LocalDateTime.now())
              .analysisData(timelineElementList)
              .build();

      timelineAnalysisRepository.save(timelineAnalysis);
    } catch (Exception e) {
      log.error("직렬화 실패: {}", e.getMessage());
      e.printStackTrace();
    }
  }

  /** 저장된 문자열을 가져와 다시 리스트 객체로 변환 */
  public List<TimelineElement> getTimelineAnalysis(Long sessionId, Long memberId) {
    try {
      TimelineAnalysis timelineAnalysis =
          timelineAnalysisRepository
              .findBySessionIdAndMemberId(sessionId, memberId)
              .orElseThrow(() -> new RuntimeException("데이터를 찾을 수 없습니다."));
      return timelineAnalysis.getAnalysisData();
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  String QUIZ_STT_FORMAT = "quiz:stt:session-id:%d";

  String generateKey(Long sessionId) {
    return QUIZ_STT_FORMAT.formatted(sessionId);
  }

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
}
