package com.edumax.molip.domain.course.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "course_learner_analysis",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_course_member",
          columnNames = {"course_id", "member_id"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseLearnerAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_learner_id")
  private Long courseLearnerId;

  @CreationTimestamp
  @Column(
      name = "created_at",
      nullable = false,
      updatable = false,
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "sum_concentrate")
  private Float sumConcentrate;

  @Column(name = "sum_attendance")
  private Integer sumAttendance;

  @Column(name = "sum_quiz_correct")
  private Float sumQuizCorrect;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Builder
  public CourseLearnerAnalysis(
      Float sumConcentrate,
      Integer sumAttendance,
      Float sumQuizCorrect,
      Course course,
      Long memberId) {
    this.sumConcentrate = (sumConcentrate != null) ? sumConcentrate : 0;
    this.sumAttendance = (sumAttendance != null) ? sumAttendance : 0;
    this.sumQuizCorrect = (sumQuizCorrect != null) ? sumQuizCorrect : 0;
    this.course = course;
    this.memberId = memberId;
  }

  /** 분석 데이터 누적 업데이트를 위한 편의 메서드 */
  public void updateAnalysis(
      float additionalConcentrate, boolean isAttendance, float additionalQuizCorrect) {
    this.sumConcentrate += additionalConcentrate;
    if (isAttendance) {
      this.sumAttendance += 1;
    }
    this.sumQuizCorrect += additionalQuizCorrect;
  }
}
