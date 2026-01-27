package com.edumax.molip.domain.session.controller;

import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;
import com.edumax.molip.domain.session.dto.response.SessionDetailResponse;
import com.edumax.molip.domain.session.dto.response.SessionSimpleResponse;
import com.edumax.molip.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

  private final SessionService sessionService;

  @PostMapping
  public ResponseEntity<Long> createSession(@RequestBody SessionCreateRequest request) {
    Long sessionId = sessionService.createSession(request);
    return ResponseEntity.ok(sessionId);
  }

  @GetMapping("/{sessionId}/simple")
  public ResponseEntity<SessionSimpleResponse> getSessionSimple(@PathVariable Long sessionId) {
    SessionSimpleResponse response = sessionService.getSessionSimple(sessionId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{sessionId}/detail")
  public ResponseEntity<SessionDetailResponse> getSessionDetail(@PathVariable Long sessionId) {
    SessionDetailResponse response = sessionService.getSessionDetail(sessionId);
    return ResponseEntity.ok(response);
  }
}

