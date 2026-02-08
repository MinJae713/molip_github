package com.edumax.molip.domain.session.dto;

public record SendConcentrateMessage(
    int timeline, boolean focusOn, boolean movement, boolean empty) {}
