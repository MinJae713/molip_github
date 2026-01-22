package com.edumax.molip.domain.workspace.entity;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.member.entity.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Workspace")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "workspace_id", nullable = false)
  private Long workspaceId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 300)
  private String description;

  @Column(name = "logo_url", length = 1000)
  private String logoUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "is_searchable", nullable = false)
  private Boolean isSearchable;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @OneToMany(mappedBy = "workspace")
  @Builder.Default
  private List<WorkspaceRegister> workspaceRegisters = new ArrayList<>();

  @OneToMany(mappedBy = "workspace")
  @Builder.Default
  private List<InviteCode> inviteCodes = new ArrayList<>();

  @OneToMany(mappedBy = "workspace")
  @Builder.Default
  private List<WorkspaceMember> workspaceMembers = new ArrayList<>();

  @OneToMany(mappedBy = "workspace")
  @Builder.Default
  private List<Course> courses = new ArrayList<>();
}
