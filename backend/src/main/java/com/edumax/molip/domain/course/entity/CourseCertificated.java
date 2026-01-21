package com.edumax.molip.domain.course.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CourseCertificated")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CourseCertificated {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_certificated_id", nullable = false)
  private Long courseCertificatedId;

  @CreationTimestamp
  @Column(name = "certificated_at", nullable = false, updatable = false)
  private LocalDateTime certificatedAt;

  @Column(nullable = false)
  private Integer score;

  @Column(name = "average_participate", nullable = false, precision = 8, scale = 2)
  private BigDecimal averageParticipate;

  @Column(name = "quiz_correct", nullable = false, precision = 8, scale = 2)
  private BigDecimal quizCorrect;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;
}

