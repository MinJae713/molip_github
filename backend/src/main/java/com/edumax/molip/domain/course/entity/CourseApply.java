package com.edumax.molip.domain.course.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "CourseApply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseApply {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_apply_id", nullable = false)
  private Long courseApplyId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "is_applied")
  private Boolean isApplied;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Course course;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private WorkspaceMember workspaceMember;
}
