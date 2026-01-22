package com.edumax.molip.domain.course.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Zzim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zzim {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "zzim_id", nullable = false)
  private Long zzimId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;
}
