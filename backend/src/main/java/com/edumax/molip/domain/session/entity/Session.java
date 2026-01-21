package com.edumax.molip.domain.session.entity;

import com.edumax.molip.domain.course.entity.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(nullable = false, length = 50)
  private String title;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private Integer number;

  @Column(name = "start_at", nullable = false)
  private LocalTime startAt;

  @Column(nullable = false)
  private LocalDate date;

  @Column(name = "end_at", nullable = false)
  private LocalTime endAt;

  @Column(name = "attendance_excused", nullable = false, precision = 8, scale = 2)
  private BigDecimal attendanceExcused;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "janus_room_id")
  private Long janusRoomId;

  @Column(length = 255)
  private String pin;
}

