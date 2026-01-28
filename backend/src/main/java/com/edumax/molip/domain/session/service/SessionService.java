package com.edumax.molip.domain.session.service;

import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;

public interface SessionService {
  Long createSession(SessionCreateRequest request, Long memberId);

  com.edumax.molip.domain.session.dto.response.SessionSimpleResponse getSessionSimple(Long sessionId, Long memberId);

  com.edumax.molip.domain.session.dto.response.SessionDetailResponse getSessionDetail(Long sessionId, Long memberId);

  com.edumax.molip.domain.session.dto.response.SessionConnectionResponse getSessionConnection(Long sessionId, Long memberId);


  void updateSession(Long sessionId, com.edumax.molip.domain.session.dto.request.SessionUpdateRequest request, Long memberId);

  void deleteSession(Long sessionId, Long memberId);

  com.edumax.molip.domain.session.dto.response.SessionStartResponse startSession(Long sessionId, Long memberId);
  
  java.util.List<com.edumax.molip.domain.session.dto.response.SessionSimpleResponse> getSessionList(Long courseId, Long memberId);

  void stopSession(Long sessionId, Long memberId);
}
