package com.edumax.molip.domain.session.service;

import com.edumax.molip.domain.session.dto.request.SessionCreateRequest;

public interface SessionService {
  Long createSession(SessionCreateRequest request);
}
