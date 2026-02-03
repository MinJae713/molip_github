package com.edumax.molip.domain.session.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EndAnalysisRequest {
  Long teacherId;
  Integer lastTimeline;
  List<PauseElement> pauseElementList;
  // public record PauseElement(int startTime, int endTime) {}
}
