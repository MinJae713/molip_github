package com.edumax.molip.domain.session.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionCreateRequest {

  private Long courseId;
  private String title;
  private String description;
  private LocalDate date;
  private LocalTime startAt;
  private LocalTime endAt;

  // Options
  @Builder.Default private Boolean useFocus = false;

  @Builder.Default private Boolean useEmotion = false;

  @Builder.Default private Boolean useQuiz = false;

  @Builder.Default private Integer quizCount = 0;

  // Optional: 퀴즈도 함께 생성할 경우 사용
  //  private List<QuizDto> quizzes;
  //
  //  @Getter
  //  @Setter
  //  @NoArgsConstructor
  //  @AllArgsConstructor
  //  @Builder
  //  @JsonIgnoreProperties(ignoreUnknown = true) // quizId 등 불필요한 필드는 무시
  //  public static class QuizDto {
  //    private String question;
  //    private Integer answerNumber; // 정답 번호 (1~4)
  //    private List<ChoiceDto> choices; // 보기 문항들 (number + content 구조)
  //  }
  //
  //  @Getter
  //  @Setter
  //  @NoArgsConstructor
  //  @AllArgsConstructor
  //  @Builder
  //  @JsonIgnoreProperties(ignoreUnknown = true) // answerId 등 불필요한 필드는 무시
  //  public static class ChoiceDto {
  //    private Integer number;
  //    private String content;
  //  }
}
