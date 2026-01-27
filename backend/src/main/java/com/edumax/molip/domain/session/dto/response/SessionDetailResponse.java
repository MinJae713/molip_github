package com.edumax.molip.domain.session.dto.response;

import com.edumax.molip.domain.quiz.entity.ExitQuiz;
import com.edumax.molip.domain.quiz.entity.ExitQuizAnswer;
import com.edumax.molip.domain.session.entity.Session;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionDetailResponse {

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
  private List<QuizInfo> quizzes;

  @Builder
  public SessionDetailResponse(Long sessionId, String title, String description, LocalDate date,
      LocalTime startAt, LocalTime endAt, Boolean useFocus, Boolean useEmotion, Boolean useQuiz,
      Integer quizCount, List<QuizInfo> quizzes) {
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
    this.quizzes = quizzes;
  }

  public static SessionDetailResponse of(Session session, List<ExitQuiz> exitQuizzes) {
    List<QuizInfo> quizInfos = exitQuizzes.stream()
        .map(QuizInfo::from)
        .collect(Collectors.toList());

    return SessionDetailResponse.builder()
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
        .quizzes(quizInfos)
        .build();
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class QuizInfo {
    private Long quizId;
    private String question;
    private Integer answerNumber;
    private List<AnswerInfo> choices;

    @Builder
    public QuizInfo(Long quizId, String question, Integer answerNumber, List<AnswerInfo> choices) {
      this.quizId = quizId;
      this.question = question;
      this.answerNumber = answerNumber;
      this.choices = choices;
    }

    public static QuizInfo from(ExitQuiz quiz) {
      return QuizInfo.builder()
          .quizId(quiz.getExitQuizId())
          .question(quiz.getQuestion())
          .answerNumber(quiz.getAnswerNumber())
          .choices(quiz.getExitQuizAnswers().stream()
              .map(AnswerInfo::from)
              .collect(Collectors.toList()))
          .build();
    }
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class AnswerInfo {
    private Long answerId;
    private Integer number;
    private String content;

    @Builder
    public AnswerInfo(Long answerId, Integer number, String content) {
      this.answerId = answerId;
      this.number = number;
      this.content = content;
    }

    public static AnswerInfo from(ExitQuizAnswer answer) {
      return AnswerInfo.builder()
          .answerId(answer.getExitQuizAnswerId())
          .number(answer.getNumber())
          .content(answer.getAnswer())
          .build();
    }
  }
}
