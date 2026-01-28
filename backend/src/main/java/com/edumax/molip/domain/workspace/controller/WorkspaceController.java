package com.edumax.molip.domain.workspace.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

  @Transactional
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
    return ResponseEntity.ok(workspaceService.findWorkspaceCoursesById(workspaceId, member.getMemberId()));
  }

  @GetMapping("/me")
  public ResponseEntity<List<FindWorkspaceResponse>> findMyWorkspaces(
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.findMyWorkspaces(member.getMemberId()));
  }

  @GetMapping("/{search_query}/search")
  public ResponseEntity<List<FindWorkspaceResponse>> searchWorkspaces(
      @PathVariable(name = "search_query") String searchQuery,
      @AuthenticationPrincipal MemberUserDetails member) {
    return ResponseEntity.ok(workspaceService.searchWorkspaces(searchQuery, member.getMemberId()));
  }
}
