package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "AiDeadzoneQuiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiDeadzoneQuiz {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "quiz_id", nullable = false)
  private Long quizId;

  @Column(name = "start_time", nullable = false)
  private Integer startTime;

  @Column(name = "end_time", nullable = false)
  private Integer endTime;

  @Column(nullable = false, length = 255)
  private String question;

  @Column(name = "answer_number", nullable = false)
  private Integer answerNumber;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private LearnerAnalysis learnerAnalysis;

  /** 퀴즈 답안 */
  @OneToMany(mappedBy = "quiz")
  @Builder.Default
  private List<AIQuizAnswer> aiQuizAnswers = new ArrayList<>();
}
