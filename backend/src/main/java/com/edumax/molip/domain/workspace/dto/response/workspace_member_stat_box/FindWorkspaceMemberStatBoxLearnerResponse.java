package com.edumax.molip.domain.workspace.dto.response.workspace_member_stat_box;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FindWorkspaceMemberStatBoxLearnerResponse extends FindWorkspaceMemberStatBoxResponse {
  private Integer learningCourseCount;
  private Integer certificatedCount;
}
