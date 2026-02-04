package com.edumax.molip.domain.quiz.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExitQuizListRequest {

  @NotNull(message = "퇴실 퀴즈 아이디는 필수값입니다.")
  @Positive(message = "퇴실 퀴즈 아이디는 양수여야 합니다.")
  private Long exitQuizId;

  @NotNull(message = "퀴즈 정답 번호는 필수값입니다.")
  @Positive(message = "퀴즈 정답 번호는 양수여야 합니다.")
  private Integer number;
}
