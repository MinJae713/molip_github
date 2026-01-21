package com.edumax.molip.domain.session.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ExitQuizType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitQuizType {

  @Id
  @GeneratedValue
  @Column(name = "exit_quiz_type_id", nullable = false)
  private Long id;

  @Column(nullable = false, length = 50)
  private String type;
}
