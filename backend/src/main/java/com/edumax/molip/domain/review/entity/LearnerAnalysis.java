package com.edumax.molip.domain.review.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "LeanerAnalysis")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LearnerAnalysis {

  @Id
  @GeneratedValue
  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "video_url")
  private String videoUrl;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}

