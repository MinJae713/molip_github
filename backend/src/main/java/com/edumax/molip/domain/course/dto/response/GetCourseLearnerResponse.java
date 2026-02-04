package com.edumax.molip.domain.course.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCourseLearnerResponse {
  private Long memberId;
  private Long workspaceMemberId;
  private Long courseLearnerId;
  private String profileImageUrl;
  private String name;
  private String position;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  //  private LocalDateTime connectedAt; // 최근 접속일
  private Double attandanceRate; // 출석률
}
