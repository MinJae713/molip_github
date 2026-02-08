package com.edumax.molip.domain.session.dto.response;

public record StudentEmotionMessage(
    String type,
    int timeline,
    String emotion1,
    float confidence1,
    String emotion2,
    float confidence2,
    String emotion3,
    float confidence3) {}
