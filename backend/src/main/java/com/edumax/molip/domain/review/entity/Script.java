package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
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
  private Integer time;

  @Column(nullable = false, length = 1000)
  private String message;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;
}
