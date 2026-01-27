package com.edumax.molip.domain.workspace.dto.response;

import com.edumax.molip.domain.course.types.CourseCategory;
import com.edumax.molip.domain.course.types.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindWorkspaceCoursesByIdResponse {
  private Long course_id;
  private Long workspace_id;
  private String title;
  private String description;
  private CourseCategory category; // MARKETING, AI, DEV, DATA
  private String sub_category;
  private Integer current_lessons; // 끝난 세션 수
  private Integer total_lessons; // 총 세션 수
  private Boolean is_liked; // 찜 여부
  private LocalDateTime d_day; // created_at
  private CourseStatus status;
  private Boolean enrollment_status; // 수강 신청 여부
}
