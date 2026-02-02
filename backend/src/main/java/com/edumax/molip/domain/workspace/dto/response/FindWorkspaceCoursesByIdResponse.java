package com.edumax.molip.domain.workspace.dto.response;

import com.edumax.molip.domain.course.types.CourseCategory;
import com.edumax.molip.domain.course.types.CourseStatus;
import com.edumax.molip.domain.workspace.types.CourseApplyStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindWorkspaceCoursesByIdResponse {
  private Long courseId;
  private Long workspaceId;
  private String title;
  private String description;
  private CourseCategory category; // MARKETING, AI, DEV, DATA
  private String subCategory;
  private Integer currentLessons; // 끝난 세션 수
  private Integer totalLessons; // 총 세션 수
  private Boolean isLiked; // 찜 여부
  private LocalDateTime dDay; // created_at
  private CourseStatus status;
  private CourseApplyStatus applyStatus; // 수강 신청 여부
}
