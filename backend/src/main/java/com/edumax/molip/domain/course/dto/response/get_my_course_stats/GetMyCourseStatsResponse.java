package com.edumax.molip.domain.course.dto.response.get_my_course_stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class GetMyCourseStatsResponse {
  protected Long courseId;
}
