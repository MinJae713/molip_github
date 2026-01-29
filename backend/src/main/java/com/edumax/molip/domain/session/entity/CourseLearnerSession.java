package com.edumax.molip.domain.session.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "CourseLearnerSession")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLearnerSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_learner_session_id", nullable = false)
  private Long courseLearnerSessionId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "exit_ticket")
  private Integer exitTicket;

  @Column(name = "is_attendance", nullable = false)
  private Boolean isAttendance = false;

  @Column(name = "attendance_time", nullable = false)
  private Integer attendanceTime = 0;

  /** 강의 수강생 (CourseLearner) FK: course_learner_id */
  @ManyToOne
  @JoinColumn(name = "course_learner_id", nullable = false)
  private CourseLearner courseLearner;

  /** 강의 세션 FK: session_id */
  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;
}
