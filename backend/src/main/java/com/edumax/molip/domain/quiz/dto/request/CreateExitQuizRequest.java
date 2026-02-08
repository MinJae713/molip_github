package com.edumax.molip.domain.quiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExitQuizRequest {

  @NotNull(message = "질문은 필수값입니다")
  private String question;

  @NotNull(message = "정답 번호는 필수값입니다.")
  @Positive(message = "정답 번호는 양수여야 합니다.")
  private Integer answerNumber;

  @NotNull(message = "답안은 최소 하나 이상 있어야 합니다.")
  @Valid
  private List<CreateExitQuizAnswerRequest> exitQuizAnswers;

  @Getter
  public static class CreateExitQuizAnswerRequest {

    @NotNull(message = "답안 번호는 필수값입니다.")
    @Positive(message = "답안 번호는 양수여야 합니다.")
    private Integer number;

    @NotNull(message = "답안 내용은 필수값입니다.")
    private String answer;
  }
}
