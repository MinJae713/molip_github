package com.edumax.molip.domain.session.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
public class SessionUpdateRequest {
  private String title;
  private String description;
  private LocalDate date;
  private LocalTime startAt;
  private LocalTime endAt;

  private Boolean useFocus;
  private Boolean useEmotion;
  private Boolean useQuiz;
  private Integer quizCount;

  // 수정할 퀴즈 목록 (기존 퀴즈를 덮어쓰기 위해 사용)
  private List<SessionCreateRequest.QuizDto> quizzes;
}
