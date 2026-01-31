package com.edumax.molip.domain.session.dto.response;

public record StudentConcentrateMessage(
    int timeline, float concentrateValue, boolean isFeverState) {}
