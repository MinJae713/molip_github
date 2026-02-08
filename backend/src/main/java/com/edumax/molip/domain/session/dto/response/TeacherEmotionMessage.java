package com.edumax.molip.domain.session.dto.response;

public record TeacherEmotionMessage(
    String type,
    int timeline,
    String emotion1,
    float confidence1,
    String emotion2,
    float confidence2,
    String emotion3,
    float confidence3,
    String emotion4,
    float confidence4,
    String emotion5,
    float confidence5,
    String emotion6,
    float confidence6,
    String emotion7,
    float confidence7) {}
