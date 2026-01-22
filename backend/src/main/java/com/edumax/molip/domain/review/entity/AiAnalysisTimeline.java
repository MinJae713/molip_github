package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "AiAnalysisTimeline")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAnalysisTimeline {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ai_analysis_timeline_id", nullable = false)
  private Long aiAnalysisTimelineId;

  @Column(nullable = false)
  private Integer time;

  @Column(nullable = false)
  private Boolean deadzone;

  @Column(name = "concentrate_value", nullable = false)
  private Integer concentrateValue;

  @Column(name = "feeling_name", nullable = false, length = 31)
  private String feelingName; // enum 인가?

  @Column(name = "en_feeling_name", nullable = false, length = 31)
  private String enFeelingName;

  @Column(name = "fever_time", nullable = false)
  private Boolean feverTime;

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
