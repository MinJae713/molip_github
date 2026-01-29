package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.workspace.dto.request.ChangeWorkspaceMemberRoleRequest;
import com.edumax.molip.domain.workspace.dto.request.CreateJoinCodeRequest;
import com.edumax.molip.domain.workspace.dto.request.CreateWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.request.InactiveWorkspaceMemberRequest;
import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.request.UpdateWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.ChangeWorkspaceMemberRoleResponse;
import com.edumax.molip.domain.workspace.dto.response.CreateJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.CreateWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.DeleteJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.DeleteWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.FindJoinCodesResponse;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceRegistersResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.InactiveWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.UpdateWorkspaceResponse;
import java.util.List;

public interface WorkspaceService {
  List<FindWorkspaceResponse> findWorkspaces(Long memberId); // 전체 워크스페이스 목록 조회 및 검색

  FindWorkspaceResponse findWorkspaceById(Long workspaceId, Long memberId); // 워크스페이스 상세 조회

  JoinWorkspaceResponse joinWorkspace(JoinWorkspaceRequest joinWorkspaceRequest); // 워크스페이스 참여 신청

  FindMyStatsResponse findMyStats(Long workspaceId); // 내 워크스페이스 통계 조회

  List<FindWorkspaceCoursesByIdResponse> findWorkspaceCoursesById(
      Long workspaceId, Long memberId); // 워크스페이스별 강의 목록 조회

  List<FindWorkspaceResponse> findMyWorkspaces(Long memberId); // 내 워크스페이스 목록 조회

  FindWorkspaceResponse searchWorkspaces(String searchQuery, Long memberId); // 워크스페이스 검색

  CreateWorkspaceResponse createWorkspace(
      CreateWorkspaceRequest createWorkspaceRequest, Long memberId); // 워크스페이스 생성

  UpdateWorkspaceResponse updateWorkspace(
      UpdateWorkspaceRequest updateWorkspaceRequest); // 워크스페이스 수정

  DeleteWorkspaceResponse deleteWorkspace(Long workspaceId); // 워크스페이스 삭제

  InactiveWorkspaceMemberResponse inactiveWorkspaceMember(
      InactiveWorkspaceMemberRequest inactiveWorkspaceMemberRequest); // 워크스페이스 멤버 비활성화

  ChangeWorkspaceMemberRoleResponse changeWorkspaceMemberRole(
      ChangeWorkspaceMemberRoleRequest changeWorkspaceMemberRoleRequest); // 워크스페이스 멤버 역할 변경

  CreateJoinCodeResponse createJoinCode(
      CreateJoinCodeRequest createJoinCodeRequest); // 워크스페이스 초대코드 생성

  List<FindJoinCodesResponse> findJoinCodes(Long workspaceId); // 워크스페이스 초대코드 목록 조회

  DeleteJoinCodeResponse deleteJoinCode(Long joinCodeId); // 워크스페이스 초대코드 삭제

  List<FindWorkspaceMemberResponse> findWorkspaceMembers(Long workspaceId); // 워크스페이스별 멤버 목록 조회

  FindWorkspaceMemberResponse findWorkspaceMember(
      Long workspaceId, Long workspaceMemberId); // 워크스페이스별 멤버 상세 정보 조회

  List<FindWorkspaceRegistersResponse> findWorkspaceRegisters(
      Long workspaceId); // 워크스페이스별 가입 신청 목록 조회
}
