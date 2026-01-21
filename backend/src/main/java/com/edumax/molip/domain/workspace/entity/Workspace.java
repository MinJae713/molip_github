package com.edumax.molip.domain.workspace.entity;

import com.edumax.molip.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Workspace")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {

  @Id
  @Column(name = "workspace_id", nullable = false)
  private Long workspaceId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private Boolean activated;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;
}

