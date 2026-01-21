package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AiDeadzoneQuiz")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiDeadzoneQuiz {

  @Id
  @GeneratedValue
  @Column(name = "quiz_id", nullable = false)
  private Long quizId;

  @Column(nullable = false)
  private Integer startTime;

  @Column(nullable = false)
  private Integer endTime;

  @Column(nullable = false, length = 255)
  private String question;

  @Column(nullable = false)
  private Integer answerNumber;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;
}

