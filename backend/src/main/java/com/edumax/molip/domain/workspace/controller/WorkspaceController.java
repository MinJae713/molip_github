package com.edumax.molip.domain.workspace.controller;

import com.edumax.molip.common.model.MemberUserDetails;
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
import com.edumax.molip.domain.workspace.dto.response.FindJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceMemberByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceRegisterResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.InactiveWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.UpdateWorkspaceResponse;
import com.edumax.molip.domain.workspace.service.WorkspaceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
  private final WorkspaceService workspaceService;

  @GetMapping
  public ResponseEntity<List<FindWorkspaceResponse>> findWorkspaces(
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findWorkspaces(member.getMemberId()));
  }

  @GetMapping("/{workspace_id}")
  public ResponseEntity<FindWorkspaceResponse> findWorkspaceById(
      @PathVariable(name = "workspace_id") Long workspaceId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findWorkspaceById(workspaceId, member.getMemberId()));
  }

  @PostMapping("/join")
  public ResponseEntity<JoinWorkspaceResponse> joinWorkspace(
      @RequestBody JoinWorkspaceRequest joinWorkspaceRequest,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.joinWorkspace(joinWorkspaceRequest));
  }

  @GetMapping("/{workspace_id}/my-stats")
  public ResponseEntity<FindMyStatsResponse> findMyStats(
      @PathVariable(name = "workspace_id") Long workspaceId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findMyStats(workspaceId));
  }

  @GetMapping("/{workspace_id}/courses")
  public ResponseEntity<List<FindWorkspaceCoursesByIdResponse>> findWorkspaceCoursesById(
      @PathVariable(name = "workspace_id") Long workspaceId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(
        workspaceService.findWorkspaceCoursesById(workspaceId, member.getMemberId()));
  }

  @GetMapping("/me")
  public ResponseEntity<List<FindWorkspaceResponse>> findMyWorkspaces(
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findMyWorkspaces(member.getMemberId()));
  }

  @GetMapping("/{search_query}/search")
  public ResponseEntity<FindWorkspaceResponse> searchWorkspaces(
      @PathVariable(name = "search_query") String searchQuery,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.searchWorkspaces(searchQuery, member.getMemberId()));
  }

  @PostMapping
  public ResponseEntity<CreateWorkspaceResponse> createWorkspace(
      @RequestBody CreateWorkspaceRequest createWorkspaceRequest,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(
        workspaceService.createWorkspace(createWorkspaceRequest, member.getMemberId()));
  }

  @PatchMapping
  public ResponseEntity<UpdateWorkspaceResponse> updateWorkspace(
      @RequestBody UpdateWorkspaceRequest updateWorkspaceRequest,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.updateWorkspace(updateWorkspaceRequest));
  }

  @DeleteMapping("/{workspace_id}")
  public ResponseEntity<DeleteWorkspaceResponse> deleteWorkspace(
      @PathVariable(name = "workspace_id") Long workspaceId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.deleteWorkspace(workspaceId, member.getMemberId()));
  }

  @PatchMapping("/inactive")
  public ResponseEntity<InactiveWorkspaceMemberResponse> inactiveWorkspaceMember(
      @RequestBody InactiveWorkspaceMemberRequest inactiveWorkspaceMemberRequest,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(
        workspaceService.inactiveWorkspaceMember(
            inactiveWorkspaceMemberRequest, member.getMemberId()));
  }

  @PatchMapping("/changeRole")
  public ResponseEntity<ChangeWorkspaceMemberRoleResponse> changeWorkspaceMemberRole(
      @RequestBody ChangeWorkspaceMemberRoleRequest changeWorkspaceMemberRoleRequest,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(
        workspaceService.changeWorkspaceMemberRole(
            changeWorkspaceMemberRoleRequest, member.getMemberId()));
  }

  @PostMapping("/joinCode")
  public ResponseEntity<CreateJoinCodeResponse> createJoinCode(
      @RequestBody CreateJoinCodeRequest createJoinCodeRequest,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(
        workspaceService.createJoinCode(createJoinCodeRequest, member.getMemberId()));
  }

  @GetMapping("/{workspace_id}/joinCode")
  public ResponseEntity<List<FindJoinCodeResponse>> findJoinCodes(
      @PathVariable(name = "workspace_id") Long workspaceId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findJoinCodes(workspaceId, member.getMemberId()));
  }

  @DeleteMapping("/joinCode/{join_code_id}")
  public ResponseEntity<DeleteJoinCodeResponse> deleteJoinCode(
      @PathVariable(name = "join_code_id") Long joinCodeId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.deleteJoinCode(joinCodeId, member.getMemberId()));
  }

  @GetMapping("/{workspaceId}/member")
  public ResponseEntity<List<FindWorkspaceMemberResponse>> findWorkspaceMembers(
      @PathVariable(name = "workspaceId") Long workspaceId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findWorkspaceMembers(workspaceId));
  }

  @GetMapping("/{workspaceId}/member/{workspaceMemberId}")
  public ResponseEntity<FindWorkspaceMemberByIdResponse> findWorkspaceMemberById(
      @PathVariable(name = "workspaceId") Long workspaceId,
      @PathVariable(name = "workspaceMemberId") Long workspaceMemberId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(
        workspaceService.findWorkspaceMemberById(workspaceId, workspaceMemberId));
  }

  @GetMapping("/{workspaceId}/register")
  public ResponseEntity<List<FindWorkspaceRegisterResponse>> findWorkspaceRegisters(
      @PathVariable(name = "workspaceId") Long workspaceId,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findWorkspaceRegisters(workspaceId));
  }
}
