package com.edumax.molip.domain.session.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
  @GeneratedValue
  @Column(name = "result_id", nullable = false)
  private Long id;

  @Column(name = "result_number")
  private Integer resultNumber;

  @Column(nullable = false)
  private Boolean result;

  @ManyToOne(optional = false)
  @JoinColumn(name = "exit_quiz_id")
  private ExitQuiz exitQuiz;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;
}
