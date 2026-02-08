package com.edumax.molip.domain.course.dto.response;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.types.CourseStatus;
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
public class GetTrainerCourseResponse {
  private Long courseId;
  private String title;
  private Integer learnerCount;
  private Integer totalSessionCount;
  private Float averageProgressRate;
  // 평균 진도율 - 강의 수강생 마다의 진도율 평균
  private CourseStatus courseStatus;

  public static GetTrainerCourseResponse from(Course course) {
    Long courseId = course.getCourseId();
    String title = course.getTitle();
    Integer learnerCount = course.getCourseLearnerCount();
    Integer totalSessionCount = course.getSessionCount();
    Float averageProgressRate = course.getAvgAttendance();
    // 수강생 세션 출석률 평균
    CourseStatus courseStatus = course.getStatus();
    return GetTrainerCourseResponse.builder()
        .courseId(courseId)
        .title(title)
        .learnerCount(learnerCount)
        .totalSessionCount(totalSessionCount)
        .averageProgressRate(averageProgressRate)
        .courseStatus(courseStatus)
        .build();
  }
}
