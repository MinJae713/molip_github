package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "FeelingGraph")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FeelingGraph {

  @Id
  @GeneratedValue
  @Column(name = "feeling_id", nullable = false)
  private Long feelingId;

  @Column(nullable = false, length = 31)
  private String name;

  @Column(nullable = false, length = 31)
  private String enName;

  private Integer value;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;
}

