package com.edumax.molip.domain.workspace.dto.response.workspace_courses;

import com.edumax.molip.domain.workspace.types.CourseApplyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FindWorkspaceCoursesByIdLearnerResponse extends FindWorkspaceCoursesByIdResponse {
  private Boolean isLiked; // 찜 여부
  private CourseApplyStatus applyStatus; // 수강 신청 여부
}
