package com.edumax.molip.domain.quiz.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindExitQuizResponse {
  private Long exitQuizId;
  private String question;
  private Integer answerNumber;
  private List<FindExitQuizAnswerResponse> exitQuizAnswers;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class FindExitQuizAnswerResponse {
    private Long exitQuizAnswerId;
    private Integer number;
    private String answer;
  }
}
