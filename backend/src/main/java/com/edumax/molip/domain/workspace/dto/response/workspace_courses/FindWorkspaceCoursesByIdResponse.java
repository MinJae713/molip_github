package com.edumax.molip.domain.workspace.dto.response.workspace_courses;

import com.edumax.molip.domain.course.types.CourseCategory;
import com.edumax.molip.domain.course.types.CourseStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class FindWorkspaceCoursesByIdResponse {
  protected Long courseId;
  protected Long workspaceId;
  protected String title;
  protected String description;
  protected CourseCategory category; // MARKETING, AI, DEV, DATA
  protected String subCategory;
  protected Integer currentLessons; // 끝난 세션 수
  protected Integer totalLessons; // 총 세션 수
  protected LocalDateTime dDay; // created_at
  protected CourseStatus status;
  //  private Boolean isLiked; // 찜 여부
  //  private CourseApplyStatus applyStatus; // 수강 신청 여부
}
