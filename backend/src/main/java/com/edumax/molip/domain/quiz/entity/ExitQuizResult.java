package com.edumax.molip.domain.quiz.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
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
@Table(name = "ExitQuizResult")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitQuizResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "exit_quiz_result_id", nullable = false)
  private Long exitQuizResultId;

  @Column(name = "result_number")
  private Integer resultNumber;

  @Column(nullable = false)
  private Boolean result;

  @ManyToOne
  @JoinColumn(name = "exit_quiz_id", nullable = false)
  private ExitQuiz exitQuiz;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;
}
