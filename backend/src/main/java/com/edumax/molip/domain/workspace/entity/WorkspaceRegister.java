package com.edumax.molip.domain.workspace.entity;

import com.edumax.molip.domain.member.entity.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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

  public enum WorkspaceRegisterStatus {
    PENDING,
    APPROVE,
    REJECTED
  }

  @Column(name = "reject_reason", length = 1000)
  private String rejectReason;

  @Column(name = "reject_type", nullable = false)
  private RejectType rejectType;

  public enum RejectType {
    AFFLIATION_MISMATCH,
    INVITE_CODE_USE_MISS,
    OVER_CAPACITY,
    ETC
  }

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;
}
