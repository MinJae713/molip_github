package com.edumax.molip.domain.workspace.entity;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.workspace.types.WorkspaceRegisterStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "WorkspaceRegister")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceRegister {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "workspace_register_id", nullable = false)
  private Long workspaceRegisterId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private WorkspaceRegisterStatus status;

  @Column(name = "reject_reason", length = 1000)
  private String rejectReason;

  @Enumerated(EnumType.STRING)
  @Column(name = "reject_type")
  private RejectType rejectType;

  public enum RejectType {
    AFFLIATION_MISMATCH,
    INVITE_CODE_USE_MISS,
    OVER_CAPACITY,
    ETC
  }

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "workspace_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Workspace workspace;
}
