package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import java.util.List;

public interface WorkspaceService {
  List<FindWorkspaceResponse> findWorkspaces(Long memberId); // 전체 워크스페이스 목록 조회 및 검색

  FindWorkspaceResponse findWorkspaceById(Long workspaceId, Long memberId); // 워크스페이스 상세 조회

  JoinWorkspaceResponse joinWorkspace(JoinWorkspaceRequest joinWorkspaceRequest); // 워크스페이스 참여 신청

  FindMyStatsResponse findMyStats(Long workspaceId); // 내 워크스페이스 통계 조회

  List<FindWorkspaceCoursesByIdResponse> findWorkspaceCoursesById(
      Long workspaceId, Long memberId); // 워크스페이스별 강의 목록 조회
}
