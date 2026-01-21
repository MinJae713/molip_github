package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AiAnalysisTimeline")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiAnalysisTimeline {

  @Id
  @GeneratedValue
  @Column(name = "concentrate_id", nullable = false)
  private Long concentrateId;

  @Column(nullable = false)
  private Integer time;

  @Column(nullable = false)
  private Boolean deadzone;

  @Column(nullable = false)
  private Integer value;

  @Column(nullable = false, length = 31)
  private String feelingName;

  @Column(nullable = false, length = 31)
  private String enFeelingName;

  @Column(nullable = false)
  private Boolean fevertime;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;
}

