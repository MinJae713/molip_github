package com.edumax.molip.domain.course.dto.response;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseCertificated;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class GetCertificatedCourseResponse {
  private Long courseCertificatedId;
  private String title;
  private String description;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime certificatedAt;

  private BigDecimal averageParticipate;
  private Long certificationId;
  private Long courseId;

  public static GetCertificatedCourseResponse from(CourseCertificated courseCertificated) {
    Course course = courseCertificated.getCourse();
    Long courseCertificatedId = courseCertificated.getCourseCertificatedId();
    String title = course.getTitle();
    String description = course.getDescription();
    LocalDateTime certificatedAt = courseCertificated.getCertificatedAt();
    BigDecimal averageParticipate = courseCertificated.getAverageParticipate();
    return GetCertificatedCourseResponse.builder()
        .courseCertificatedId(courseCertificatedId)
        .title(title)
        .description(description)
        .certificatedAt(certificatedAt)
        .averageParticipate(averageParticipate)
        .certificationId(-9999L)
        .courseId(course.getCourseId())
        .build();
  }
}
