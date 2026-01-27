package com.edumax.molip.domain.session.dto.response;

import com.edumax.molip.domain.session.entity.Session;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionSimpleResponse {

  private Long sessionId;
  private String title;
  private String description;
  private LocalDate date;
  private LocalTime startAt;
  private LocalTime endAt;
  private Boolean useFocus;
  private Boolean useEmotion;
  private Boolean useQuiz;
  private Integer quizCount;

  @Builder
  public SessionSimpleResponse(Long sessionId, String title, String description, LocalDate date,
      LocalTime startAt, LocalTime endAt, Boolean useFocus, Boolean useEmotion, Boolean useQuiz, Integer quizCount) {
    this.sessionId = sessionId;
    this.title = title;
    this.description = description;
    this.date = date;
    this.startAt = startAt;
    this.endAt = endAt;
    this.useFocus = useFocus;
    this.useEmotion = useEmotion;
    this.useQuiz = useQuiz;
    this.quizCount = quizCount;
  }

  public static SessionSimpleResponse from(Session session) {
    return SessionSimpleResponse.builder()
        .sessionId(session.getSessionId())
        .title(session.getTitle())
        .description(session.getDescription())
        .date(session.getDate())
        .startAt(session.getStartAt())
        .endAt(session.getEndAt())
        .useFocus(session.getUseFocus())
        .useEmotion(session.getUseEmotion())
        .useQuiz(session.getUseQuiz())
        .quizCount(session.getQuizCount())
        .build();
  }
}
