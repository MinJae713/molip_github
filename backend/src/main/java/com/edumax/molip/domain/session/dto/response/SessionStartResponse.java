package com.edumax.molip.domain.session.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionStartResponse {
  private Long sessionId;
  private Long janusRoomId;
  private String pin;
}
