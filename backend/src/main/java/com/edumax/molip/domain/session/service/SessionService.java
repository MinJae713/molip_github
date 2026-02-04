package com.edumax.molip.domain.session.service;

import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;
import com.edumax.molip.domain.session.dto.response.SessionConnectionResponse;
import com.edumax.molip.domain.session.dto.response.SessionDetailResponse;
import com.edumax.molip.domain.session.dto.response.SessionSimpleResponse;
import com.edumax.molip.domain.session.dto.response.SessionStartResponse;
import java.util.List;

public interface SessionService {
  void startSession(String sessionId);

  void endSession(String sessionId);

  Long createSession(SessionCreateRequest request, Long memberId);

  SessionSimpleResponse getSessionSimple(Long sessionId, Long memberId);

  SessionDetailResponse getSessionDetail(Long sessionId, Long memberId);

  SessionConnectionResponse getSessionConnection(Long sessionId, Long memberId);

  void updateSession(
      Long sessionId,
      com.edumax.molip.domain.session.dto.request.SessionUpdateRequest request,
      Long memberId);

  void deleteSession(Long sessionId, Long memberId);

  SessionStartResponse startSession(Long sessionId, Long memberId);

  List<SessionSimpleResponse> getSessionList(Long courseId, Long memberId);

  void stopSession(Long sessionId, Long memberId);

  float getConcentrateDiff(boolean empty, boolean focusOn, boolean movement);

  float changeConcentrate(String sessionId, Long memberId, int timeline, float diff);

  boolean initializeOrNot(String sessionId, Long memberId, int timeline);

  boolean isFeverState(boolean empty, boolean focusOn, boolean movement);

  boolean handleFeverState(String sessionId, Long memberId, int timeline);

  List<com.edumax.molip.domain.session.dto.response.ParticipantResponse> getParticipants(
      Long sessionId);
}
