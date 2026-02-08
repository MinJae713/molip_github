package com.edumax.molip.domain.workspace.dto.response.find_workspace_response;

import com.edumax.molip.domain.workspace.entity.Workspace;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FindWorkspaceResponseAdmin extends FindWorkspaceResponse {
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  public static FindWorkspaceResponseAdmin from(Workspace workspace) {
    Long workspaceId = workspace.getWorkspaceId();
    String name = workspace.getName();
    String logoUrl = workspace.getLogoUrl();
    String description = workspace.getDescription();
    Integer memberCount = workspace.getWorkspaceMembers().size();
    Integer courseCount = workspace.getCourses().size();
    return FindWorkspaceResponseAdmin.builder()
        .workspaceId(workspaceId)
        .name(name)
        .logoUrl(logoUrl)
        .description(description)
        .memberCount(memberCount)
        .courseCount(courseCount)
        .createdAt(workspace.getCreatedAt())
        .build();
  }
}
