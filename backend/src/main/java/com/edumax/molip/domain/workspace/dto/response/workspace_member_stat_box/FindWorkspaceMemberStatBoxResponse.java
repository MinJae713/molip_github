package com.edumax.molip.domain.workspace.dto.response.workspace_member_stat_box;

import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class FindWorkspaceMemberStatBoxResponse {
  protected Long workspaceMemberId;
  protected Long workspaceId;
  protected WorkspaceMemberRole role;
  protected Integer todaySessionCount;
}
