package com.edumax.molip.domain.session.service;

import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;

public interface SessionService {
  Long createSession(SessionCreateRequest request);

  com.edumax.molip.domain.session.dto.response.SessionSimpleResponse getSessionSimple(Long sessionId);

  com.edumax.molip.domain.session.dto.response.SessionDetailResponse getSessionDetail(Long sessionId);
}
