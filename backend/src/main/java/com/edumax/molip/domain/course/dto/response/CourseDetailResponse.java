package com.edumax.molip.domain.course.dto.response;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.types.CourseCategory;
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
public class CourseDetailResponse {

  private Long courseId;
  private String title;
  private String description;
  private String subCategory;
  private CourseCategory category;
  private CourseStatus status;

  private Integer maxLearner;
  private Integer period;

  private Boolean isEngageCheck;
  private Boolean isSentimentAnalysis;

  private Integer attendance; // percent로 받음
  private Integer engagement;
  private Integer exitQuiz;

  private Long workspaceId;
  private String ownerName; // 강사 이름
  private Boolean isOwner; // 내가 만든 코스인지 여부

  public static CourseDetailResponse from(Course course) {
    return from(course, -1L); // 기본값: 주인을 알 수 없음 OR false
  }

  public static CourseDetailResponse from(Course course, Long memberId) {
    boolean isOwner = course.getLeaner().getMember().getId().equals(memberId);
    return CourseDetailResponse.builder()
        .courseId(course.getCourseId())
        .title(course.getTitle())
        .description(course.getDescription())
        .subCategory(course.getSubCategory())
        .category(course.getCategory())
        .status(course.getStatus())
        .maxLearner(course.getMaxLearner())
        .period(course.getPeriod())
        .isEngageCheck(course.getIsEngageCheck())
        .isSentimentAnalysis(course.getIsSentimentAnalysis())
        .attendance(course.getAttendance())
        .engagement(course.getEngagement())
        .exitQuiz(course.getExitQuiz())
        .workspaceId(course.getWorkspace().getWorkspaceId())
        .ownerName(course.getLeaner().getMember().getName())
        .isOwner(isOwner)
        .build();
  }
}
