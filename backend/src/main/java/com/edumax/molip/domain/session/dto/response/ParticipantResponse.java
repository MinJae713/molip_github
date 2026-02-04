package com.edumax.molip.domain.session.dto.response;

import lombok.Builder;

@Builder
public record ParticipantResponse(
    Long memberId, // 멤버 고유 ID (DB PK)
    String name, // 멤버 이름 (표시용)
    Long feedId, // Janus VideoRoom Feed ID (비디오 스트림 식별자)
    String role // 역할 (TEACHER, STUDENT)
    ) {}
