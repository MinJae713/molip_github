package com.edumax.molip.domain.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RejectType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reject_type_id", nullable = false)
  private Long rejectTypeId;

  @Column(name = "reject_type_name", nullable = false, length = 50)
  private String rejectTypeName;
}
