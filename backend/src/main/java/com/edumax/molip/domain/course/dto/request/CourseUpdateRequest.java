package com.edumax.molip.domain.course.dto.request;

import com.edumax.molip.domain.course.types.CourseCategory;
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
public class CourseUpdateRequest {

  private String title;
  private String description;
  private String subCategory;
  private CourseCategory category;

  private Integer maxLearner;
  private Integer period;

  // Options
  private Boolean isEngageCheck;
  private Boolean isSentimentAnalysis;

  // Completion Criteria
  private Integer attendance;
  private Integer engagement;
  private Integer exitQuiz;
}
