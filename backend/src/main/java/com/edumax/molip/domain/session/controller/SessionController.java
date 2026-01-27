package com.edumax.molip.domain.session.controller;

import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;
import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;
import com.edumax.molip.domain.session.dto.request.SessionUpdateRequest;
import com.edumax.molip.domain.session.dto.response.SessionDetailResponse;
import com.edumax.molip.domain.session.dto.response.SessionSimpleResponse;
import com.edumax.molip.domain.session.dto.response.SessionStartResponse;
import com.edumax.molip.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

  private final SessionService sessionService;

  @PostMapping
  public ResponseEntity<Long> createSession(
      @RequestBody SessionCreateRequest request,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    
    Long sessionId = sessionService.createSession(request, userDetails.getMemberId());
    return ResponseEntity.ok(sessionId);
  }
  
  @GetMapping
  public ResponseEntity<java.util.List<SessionSimpleResponse>> getSessionList(
      @org.springframework.web.bind.annotation.RequestParam Long courseId,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    
    return ResponseEntity.ok(sessionService.getSessionList(courseId, userDetails.getMemberId()));
  }

  @GetMapping("/{sessionId}/simple")
  public ResponseEntity<SessionSimpleResponse> getSessionSimple(
      @PathVariable Long sessionId,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    SessionSimpleResponse response = sessionService.getSessionSimple(sessionId, userDetails.getMemberId());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{sessionId}/detail")
  public ResponseEntity<SessionDetailResponse> getSessionDetail(
      @PathVariable Long sessionId,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    SessionDetailResponse response = sessionService.getSessionDetail(sessionId, userDetails.getMemberId());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{sessionId}")
  public ResponseEntity<String> updateSession(
      @PathVariable Long sessionId,
      @RequestBody SessionUpdateRequest request,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    
    try {
      sessionService.updateSession(sessionId, request, userDetails.getMemberId());
      return ResponseEntity.ok("수정 완료");
    } catch (Exception e) {
      return ResponseEntity.status(400).body("오류 발생");
    }
  }

  @DeleteMapping("/{sessionId}")
  public ResponseEntity<String> deleteSession(
      @PathVariable Long sessionId,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    try {
      sessionService.deleteSession(sessionId, userDetails.getMemberId());
      return ResponseEntity.ok("삭제 완료");
    } catch (Exception e) {
      return ResponseEntity.status(400).body("오류 발생");
    }
  }

  @PostMapping("/{sessionId}/start")
  public ResponseEntity<SessionStartResponse> startSession(
      @PathVariable Long sessionId,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
      
    SessionStartResponse response = sessionService.startSession(sessionId, userDetails.getMemberId());
    return ResponseEntity.ok(response);
  }
}

