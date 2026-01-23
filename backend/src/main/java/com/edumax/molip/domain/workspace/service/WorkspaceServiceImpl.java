package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspacesResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.repository.InviteCodeRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceMemberRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceRegisterRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
  private final InviteCodeRepository inviteCodeRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final WorkspaceRegisterRepository workspaceRegisterRepository;
  private final WorkspaceRepository workspaceRepository;

  @Override
  public FindWorkspacesResponse findWorkspaces() {
    return null;
  }

  @Override
  public FindWorkspaceByIdResponse findWorkspaceById(Long workspaceId) {
    return null;
  }

  @Override
  public JoinWorkspaceResponse joinWorkspace(JoinWorkspaceRequest joinWorkspaceRequest) {
    return null;
  }

  @Override
  public FindMyStatsResponse findMyStats(Long workspaceId) {
    return null;
  }

  @Override
  public FindWorkspaceCoursesByIdResponse findWorkspaceCoursesById(Long workspaceId) {
    return null;
  }
}
