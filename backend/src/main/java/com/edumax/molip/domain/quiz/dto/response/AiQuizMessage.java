package com.edumax.molip.domain.quiz.dto.response;

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
public class AiQuizMessage {
  private String question;
  private String answer1;
  private String answer2;
  private String answer3;
  private String answer4;
  private int correctAnswer;
  private String explanation;
}
