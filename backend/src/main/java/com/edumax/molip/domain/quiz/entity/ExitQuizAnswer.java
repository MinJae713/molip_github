package com.edumax.molip.domain.quiz.entity;

import com.edumax.molip.domain.quiz.dto.request.CreateExitQuizRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ExitQuizAnswer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitQuizAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "exit_quiz_answer_id", nullable = false)
  private Long exitQuizAnswerId;

  @Column(nullable = false)
  private Integer number;

  @Column(nullable = false, length = 255)
  private String answer;

  @ManyToOne(optional = false)
  @JoinColumn(name = "exit_quiz_id")
  private ExitQuiz exitQuiz;

  public static ExitQuizAnswer from(
      CreateExitQuizRequest.CreateExitQuizAnswerRequest exitQuizAnswer, ExitQuiz exitQuiz) {
    Integer number = exitQuizAnswer.getNumber();
    String answer = exitQuizAnswer.getAnswer();
    return ExitQuizAnswer.builder().number(number).answer(answer).exitQuiz(exitQuiz).build();
  }
}
