package com.edumax.molip.domain.workspace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "InviteCode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "invitecode_id", nullable = false)
  private Long inviteCodeId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  @Column(name = "code_name", nullable = false, length = 50)
  private String codeName;

  @Column(name = "identify_value", nullable = false, length = 50)
  private String identifyValue;

  @ManyToOne
  @JoinColumn(name = "workspace_id", nullable = false)
  private Workspace workspace;
}
