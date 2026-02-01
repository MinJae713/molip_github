package com.edumax.molip.domain.session.dto.response;

import com.edumax.molip.domain.session.handler.AnalysisManager;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class TeacherAnalysisMessage {
  private TeacherConcentrateMessage concentrateMessage;
  private TeacherEmotionMessage emotionMessage;
  private List<AnalysisManager.StateChange> stateChangeList;
}
