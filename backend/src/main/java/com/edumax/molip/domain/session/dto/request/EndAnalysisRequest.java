package com.edumax.molip.domain.session.dto.request;

import java.util.List;

public record EndAnalysisRequest(List<PauseElement> pauseElementList) {}

record PauseElement(int startTime, int endTime) {}
