package com.edumax.molip.domain.session.dto.response;

import com.edumax.molip.domain.session.entity.Session;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionConnectionResponse {
    private Long sessionId;
    private Long janusRoomId;
    private String pin;

    public static SessionConnectionResponse from(Session session) {
        return SessionConnectionResponse.builder()
                .sessionId(session.getSessionId())
                .janusRoomId(session.getJanusRoomId())
                .pin(session.getPin())
                .build();
    }
}
