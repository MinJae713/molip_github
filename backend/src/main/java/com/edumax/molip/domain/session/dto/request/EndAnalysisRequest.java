package com.edumax.molip.domain.session.dto.request;

import java.util.List;

public record EndAnalysisRequest(
    Long teacherId, String videoUrl, int lastTimeline, List<PauseElement> pauseElementList) {}
