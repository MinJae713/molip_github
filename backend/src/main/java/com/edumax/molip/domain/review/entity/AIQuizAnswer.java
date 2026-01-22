package com.edumax.molip.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AIQuizAnswer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIQuizAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ai_quiz_answer_id", nullable = false)
  private Long aiQuizAnswerId;

  @Column(nullable = false)
  private Integer number;

  @Column(nullable = false, length = 255)
  private String answer;

  @ManyToOne
  @JoinColumn(name = "quiz_id", nullable = false)
  private AiDeadzoneQuiz quiz;
}
