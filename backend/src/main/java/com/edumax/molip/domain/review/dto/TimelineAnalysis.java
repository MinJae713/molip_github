package com.edumax.molip.domain.review.dto;

import com.edumax.molip.domain.review.entity.TimelineElement;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "timeline_analysis")
@CompoundIndex(
    name = "session_member_idx",
    def = "{'sessionId': 1, 'memberId': 1}",
    unique = true) // 추가
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TimelineAnalysis {
  @Id private String id;
  private Long sessionId;
  private Long memberId;
  private LocalDateTime saveTime;
  private List<TimelineElement> analysisData;
}
