package com.edumax.molip.domain.session.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ExitQuiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitQuiz {

  @Id
  @GeneratedValue
  @Column(name = "exit_quiz_id", nullable = false)
  private Long id;

  @Column(nullable = false, length = 255)
  private String question;

  @Column(name = "answer_number", nullable = false)
  private Integer answerNumber;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "session_id")
  private Session session;

  @OneToOne(optional = false)
  @JoinColumn(name = "exit_quiz_type_id")
  private ExitQuizType exitQuizType;
}
