package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "EmotionGraph")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionGraph {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "emotion_graph_id", nullable = false)
  private Long emotionGraphId;

  @Column(nullable = false, length = 31)
  private String name;

  @Column(nullable = false)
  private Float value;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private LearnerAnalysis learnerAnalysis;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = true)
  private WorkspaceMember workspaceMember;

  @Column(nullable = false)
  private Long memberId;
}
