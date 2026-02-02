package com.edumax.molip.domain.session.dto;

public record ConcentrateSaveEvent(
    Long memberId, String sessionId, int timeline, float concentrateValue, boolean feverTime) {}
