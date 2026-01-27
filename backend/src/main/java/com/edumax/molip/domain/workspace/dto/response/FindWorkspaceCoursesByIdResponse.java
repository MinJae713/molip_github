package com.edumax.molip.domain.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindWorkspaceCoursesByIdResponse {
  private Long course_id;
  private Long workspace_id;
  private String title;
  private String description;
  private String category;
  private String sub_category;
  private String tag;
  private String progress_rate;
  private Integer current_lessons;
  private Integer total_lessons;
  private Boolean is_mandatory;
  private Boolean is_liked;
  private Integer d_day;
  private String status;
  private String enrollment_status;
}
