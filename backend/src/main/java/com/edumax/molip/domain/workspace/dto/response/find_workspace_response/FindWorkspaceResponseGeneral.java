package com.edumax.molip.domain.workspace.dto.response.find_workspace_response;

import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FindWorkspaceResponseGeneral extends FindWorkspaceResponse {
  private String myStatus;
  private WorkspaceMemberRole myRole;
  private Long workspaceMemberId;

  public static FindWorkspaceResponseGeneral from(
      Workspace workspace, String myStatus, WorkspaceMemberRole myRole, Long workspaceMemberId) {
    Long workspaceId = workspace.getWorkspaceId();
    String name = workspace.getName();
    String description = workspace.getDescription();
    String logoUrl = workspace.getLogoUrl();
    Integer memberCount = workspace.getWorkspaceMembers().size();
    Integer courseCount = workspace.getCourses().size();

    return FindWorkspaceResponseGeneral.builder()
        .workspaceId(workspaceId)
        .name(name)
        .logoUrl(logoUrl)
        .description(description)
        .memberCount(memberCount)
        .courseCount(courseCount)
        .myStatus(myStatus)
        .myRole(myRole)
        .workspaceMemberId(workspaceMemberId)
        .build();
  }
}
