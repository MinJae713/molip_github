package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspacesResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;

public interface WorkspaceService {
  FindWorkspacesResponse findWorkspaces(); // 전체 워크스페이스 목록 조회 및 검색

  FindWorkspaceByIdResponse findWorkspaceById(Long workspaceId); // 워크스페이스 상세 조회

  JoinWorkspaceResponse joinWorkspace(JoinWorkspaceRequest joinWorkspaceRequest); // 워크스페이스 참여 신청

  FindMyStatsResponse findMyStats(Long workspaceId); // 내 워크스페이스 통계 조회

  FindWorkspaceCoursesByIdResponse findWorkspaceCoursesById(Long workspaceId); // 워크스페이스별 강의 목록 조회
}
