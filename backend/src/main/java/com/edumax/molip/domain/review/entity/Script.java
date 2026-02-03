package com.edumax.molip.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Script")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Script {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "script_id", nullable = false)
  private Long scriptId;

  @Column(nullable = false)
  private Integer timeline;

  @Column(nullable = false, length = 255)
  private String message;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private LearnerAnalysis learnerAnalysis;
}
