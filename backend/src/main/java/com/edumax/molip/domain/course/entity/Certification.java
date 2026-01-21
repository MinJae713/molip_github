package com.edumax.molip.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Certification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Certification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "certification_id", nullable = false)
  private Long certificationId;

  @Column(name = "qualification_id", nullable = false, length = 100)
  private String qualificationId;

  @Column(name = "certificated_hash", nullable = false, length = 100)
  private String certificatedHash;

  @Column(name = "certificated_at", nullable = false)
  private LocalDateTime certificatedAt;

  @Column(name = "issuing_agency", nullable = false, length = 50)
  private String issuingAgency;

  @OneToOne
  @JoinColumn(name = "course_certificated_id", nullable = false)
  private CourseCertificated courseCertificated;
}

