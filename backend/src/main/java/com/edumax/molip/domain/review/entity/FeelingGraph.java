package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "FeelingGraph")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeelingGraph {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "feeling_graph_id", nullable = false)
  private Long feelingGraphId;

  @Column(nullable = false, length = 31)
  private String name;

  @Column(name = "en_name", nullable = false, length = 31)
  private String enName;

  private Integer value;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private LearnerAnalysis learnerAnalysis;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;
}
