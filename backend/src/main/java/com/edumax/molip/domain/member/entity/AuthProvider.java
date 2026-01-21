package com.edumax.molip.domain.member.entity;

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
@Table(name = "AuthProvider")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "provider_id", nullable = false)
  private Long providerId;

  @Column(name = "provider_name", nullable = false, length = 100)
  private String providerName;
}

