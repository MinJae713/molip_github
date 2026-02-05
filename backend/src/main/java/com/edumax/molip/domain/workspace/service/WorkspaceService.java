package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.workspace.dto.request.ActivateWorkspaceMemberRequest;
import com.edumax.molip.domain.workspace.dto.request.ChangeWorkspaceMemberRoleRequest;
import com.edumax.molip.domain.workspace.dto.request.CreateJoinCodeRequest;
import com.edumax.molip.domain.workspace.dto.request.CreateWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.edumax.molip.domain.workspace.dto.request.UpdateWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.ActivateWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.ChangeWorkspaceMemberRoleResponse;
import com.edumax.molip.domain.workspace.dto.response.CreateJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.CreateWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.DeleteJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.DeleteWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.FindJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceMemberByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceRegisterResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.UpdateWorkspaceMemberRoleResponse;
import com.edumax.molip.domain.workspace.dto.response.UpdateWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.find_workspace_response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.workspace_courses.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.workspace_member_stat_box.FindWorkspaceMemberStatBoxResponse;
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

  DeleteWorkspaceResponse deleteWorkspace(Long workspaceId, Long memberId); // 워크스페이스 삭제

  ActivateWorkspaceMemberResponse activateWorkspaceMember(
      ActivateWorkspaceMemberRequest inactiveWorkspaceMemberRequest,
      Long memberId); // 워크스페이스 멤버 비활성화

  ChangeWorkspaceMemberRoleResponse changeWorkspaceMemberRole(
      ChangeWorkspaceMemberRoleRequest changeWorkspaceMemberRoleRequest,
      Long memberId); // 워크스페이스 멤버 역할 변경

  CreateJoinCodeResponse createJoinCode(
      CreateJoinCodeRequest createJoinCodeRequest, Long memberId); // 워크스페이스 초대코드 생성

  List<FindJoinCodeResponse> findJoinCodes(Long workspaceId, Long memberId); // 워크스페이스 초대코드 목록 조회

  DeleteJoinCodeResponse deleteJoinCode(Long joinCodeId, Long memberId); // 워크스페이스 초대코드 삭제

  List<FindWorkspaceMemberResponse> findWorkspaceMembers(Long workspaceId); // 워크스페이스별 멤버 목록 조회

  FindWorkspaceMemberByIdResponse findWorkspaceMemberById(
      Long workspaceId, Long workspaceMemberId); // 워크스페이스별 멤버 상세 정보 조회

  List<FindWorkspaceRegisterResponse> findWorkspaceRegisters(
      Long workspaceId); // 워크스페이스별 가입 신청 목록 조회

  FindWorkspaceMemberStatBoxResponse findWorkspaceMemberStatBox(
      Long workspaceId, Long memberId); // 워크스페이스 멤버 상단 통계 박스 조회

  UpdateWorkspaceMemberRoleResponse updateWorkspaceMemberRole(
      UpdateWorkspaceMemberRoleRequest updateWorkspaceMemberRoleRequest,
      Long memberId); // 워크스페이스 멤버 역할 변경
}
