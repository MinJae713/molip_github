package com.edumax.molip.domain.session.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.session.dto.ConcentrateSaveEvent;
import com.edumax.molip.domain.session.dto.SendConcentrateMessage;
import com.edumax.molip.domain.session.dto.request.EndAnalysisRequest;
import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;
import com.edumax.molip.domain.session.dto.request.SessionUpdateRequest;
import com.edumax.molip.domain.session.dto.response.SessionDetailResponse;
import com.edumax.molip.domain.session.dto.response.SessionSimpleResponse;
import com.edumax.molip.domain.session.dto.response.SessionStartResponse;
import com.edumax.molip.domain.session.dto.response.StudentConcentrateMessage;
import com.edumax.molip.domain.session.service.SessionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
@RequiredArgsConstructor
@RequestMapping("/api/session")
public class SessionController {
  private final ApplicationEventPublisher eventPublisher;
  private final SimpMessagingTemplate messagingTemplate;
  private final SessionService sessionService;

  @MessageMapping("/concentrate/{sessionId}/{memberId}")
  public void processConcentration(
      @DestinationVariable("sessionId") String sessionId, // PathVariable 대신 DestinationVariable 사용
      @DestinationVariable("memberId") Long memberId,
      @Payload SendConcentrateMessage sendMessage) {

    if (sendMessage.empty()) {
      boolean initialized =
          sessionService.initializeOrNot(sessionId, memberId, sendMessage.timeline());
      if (initialized) {
        StudentConcentrateMessage message =
            new StudentConcentrateMessage(sendMessage.timeline(), 30f, false, true);
        messagingTemplate.convertAndSend("/topic/analysis/" + sessionId + "/" + memberId, message);
        eventPublisher.publishEvent(
            new ConcentrateSaveEvent(
                memberId, sessionId, sendMessage.timeline(), true, 30f, false));
        return;
      }
    }

    float diff =
        sessionService.getConcentrateDiff(
            sendMessage.empty(), sendMessage.focusOn(), sendMessage.movement());
    float concentrateValue =
        sessionService.changeConcentrate(sessionId, memberId, sendMessage.timeline(), diff);

    boolean feverTime = false;
    boolean isFever =
        sessionService.isFeverState(
            sendMessage.empty(), sendMessage.focusOn(), sendMessage.movement());
    if (isFever) {
      feverTime = sessionService.handleFeverState(sessionId, memberId, sendMessage.timeline());
    }

    StudentConcentrateMessage message =
        new StudentConcentrateMessage(sendMessage.timeline(), concentrateValue, feverTime, false);

    messagingTemplate.convertAndSend("/topic/analysis/" + sessionId + "/" + memberId, message);
    eventPublisher.publishEvent(
        new ConcentrateSaveEvent(
            memberId, sessionId, sendMessage.timeline(), false, concentrateValue, feverTime));
  }

  @PostMapping("/start/{sessionId}")
  public ResponseEntity<?> startSession(@PathVariable String sessionId) {
    // 강의 시작할때 필요한 작업
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "강의 시작"));
  }

  @PostMapping("/end/{sessionId}")
  public ResponseEntity<?> endSession(@PathVariable String sessionId) {
    sessionService.endSession(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "강의 종료"));
  }

  @PostMapping("/start/analysis/{sessionId}")
  public ResponseEntity<Map<String, String>> startAnalysis(@PathVariable String sessionId) {
    sessionService.startAnalysis(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "분석 시작"));
  }

  @PostMapping("/end/analysis/{sessionId}")
  public ResponseEntity<?> endAnalysis(
      @PathVariable String sessionId, @RequestBody(required = false) EndAnalysisRequest request) {
    sessionService.endAnalysis(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "분석 종료"));
  }

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
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    SessionSimpleResponse response =
        sessionService.getSessionSimple(sessionId, userDetails.getMemberId());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{sessionId}/detail")
  public ResponseEntity<SessionDetailResponse> getSessionDetail(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    SessionDetailResponse response =
        sessionService.getSessionDetail(sessionId, userDetails.getMemberId());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{sessionId}/connection")
  public ResponseEntity<com.edumax.molip.domain.session.dto.response.SessionConnectionResponse>
      getSessionConnection(
          @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {

    return ResponseEntity.ok(
        sessionService.getSessionConnection(sessionId, userDetails.getMemberId()));
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
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    try {
      sessionService.deleteSession(sessionId, userDetails.getMemberId());
      return ResponseEntity.ok("삭제 완료");
    } catch (Exception e) {
      return ResponseEntity.status(400).body("오류 발생");
    }
  }

  @PostMapping("/{sessionId}/start")
  public ResponseEntity<SessionStartResponse> startSession(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {

    SessionStartResponse response =
        sessionService.startSession(sessionId, userDetails.getMemberId());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{sessionId}/stop")
  public ResponseEntity<String> stopSession(
      @PathVariable Long sessionId, @AuthenticationPrincipal MemberUserDetails userDetails) {

    try {
      sessionService.stopSession(sessionId, userDetails.getMemberId());
      return ResponseEntity.ok("세션이 종료되었습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }
  }
}
