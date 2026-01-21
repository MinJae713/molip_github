package com.edumax.molip.domain.member.entity;

import com.edumax.molip.domain.member.types.MemberGender;
import com.edumax.molip.domain.member.types.MemberRole;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import com.edumax.molip.domain.workspace.entity.WorkspaceRegister;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Column(nullable = false, length = 50)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column
  private Integer age;

  @Column(length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(length = 6)
  private MemberGender gender;

  @Column(name = "phone_number", length = 13)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MemberRole role;

  @Column(length = 50)
  private String position;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToOne(optional = false)
  @JoinColumn(name = "provider_id", nullable = false)
  private AuthProvider authProvider;

  /** 워크스페이스 가입 신청 */
  @OneToMany(mappedBy = "member")
  @Builder.Default
  private List<WorkspaceRegister> workspaceRegisters = new ArrayList<>();

  /** 생성한 워크스페이스 */
  @OneToMany(mappedBy = "owner")
  @Builder.Default
  private List<Workspace> workspaces = new ArrayList<>();

  /** 워크스페이스 참여 정보 */
  @OneToMany(mappedBy = "member")
  @Builder.Default
  private List<WorkspaceMember> workspaceMembers = new ArrayList<>();
}
