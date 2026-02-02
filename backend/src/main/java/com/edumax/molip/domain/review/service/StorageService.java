package com.edumax.molip.domain.review.service;

import com.edumax.molip.domain.review.dto.TimelineAnalysis;
import com.edumax.molip.domain.review.entity.TimelineElement;
import com.edumax.molip.domain.review.repository.TimelineAnalysisRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
  private final TimelineAnalysisRepository timelineAnalysisRepository;

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
}
