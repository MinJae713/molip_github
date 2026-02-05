package com.edumax.molip.domain.workspace.dto.response.find_workspace_response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class FindWorkspaceResponse {
  protected Long workspaceId;
  protected String name;
  protected String logoUrl;
  protected String description;
  protected Integer memberCount;
  protected Integer courseCount;
}
