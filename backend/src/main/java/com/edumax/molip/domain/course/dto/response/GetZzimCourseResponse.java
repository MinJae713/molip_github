package com.edumax.molip.domain.course.dto.response;

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
public class GetZzimCourseResponse {
  private Long zzimId;
  private String title;
  private String description;
  private String subCategory;
  private Long courseTime;
  private Integer learnerCount;
  private Boolean isApplied;
}
