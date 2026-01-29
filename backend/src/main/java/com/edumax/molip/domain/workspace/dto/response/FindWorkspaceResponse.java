package com.edumax.molip.domain.workspace.dto.response;

import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindWorkspaceResponse {
  private Long workspaceId;
  private String name;
  private String logoUrl;
  private String description;
  private Integer memberCount;
  private Integer courseCount;
  private String myStatus;
  private WorkspaceMemberRole myRole;
}
