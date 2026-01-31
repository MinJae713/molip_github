package com.edumax.molip.domain.session.dto;

public record ConcentrateSaveEvent(
    Long memberId,
    String sessionId,
    int timeline,
    boolean emptyState,
    float concentrateValue,
    boolean feverTime) {}
