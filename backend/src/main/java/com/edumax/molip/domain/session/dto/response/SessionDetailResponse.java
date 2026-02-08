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
  private Long trainerId;
  private List<QuizInfo> quizzes;

  @Builder
  public SessionDetailResponse(
      Long sessionId,
      String title,
      String description,
      LocalDate date,
      LocalTime startAt,
      LocalTime endAt,
      Boolean useFocus,
      Boolean useEmotion,
      Boolean useQuiz,
      Integer quizCount,
      Long trainerId,
      List<QuizInfo> quizzes) {
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
    this.trainerId = trainerId;
  }

  public static SessionDetailResponse of(Session session, List<ExitQuiz> exitQuizzes) {
    List<QuizInfo> quizInfos =
        exitQuizzes.stream().map(QuizInfo::from).collect(Collectors.toList());

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
        .trainerId(session.getTrainerId())
        .build();
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class QuizInfo {
    private String question;
    private Integer answerNumber;
    private List<AnswerInfo> choices;

    @Builder
    public QuizInfo(String question, Integer answerNumber, List<AnswerInfo> choices) {
      this.question = question;
      this.answerNumber = answerNumber;
      this.choices = choices;
    }

    public static QuizInfo from(ExitQuiz quiz) {
      return QuizInfo.builder()
          .question(quiz.getQuestion())
          .answerNumber(quiz.getAnswerNumber())
          .choices(
              quiz.getExitQuizAnswers().stream().map(AnswerInfo::from).collect(Collectors.toList()))
          .build();
    }
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class AnswerInfo {
    private Integer number;
    private String content;

    @Builder
    public AnswerInfo(Integer number, String content) {
      this.number = number;
      this.content = content;
    }

    public static AnswerInfo from(ExitQuizAnswer answer) {
      return AnswerInfo.builder().number(answer.getNumber()).content(answer.getAnswer()).build();
    }
  }
}
