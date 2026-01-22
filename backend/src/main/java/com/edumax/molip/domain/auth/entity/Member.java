package com.edumax.molip.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(nullable = false, length = 50)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, length = 63)
  private String name;

  @Column(nullable = true, length = 255)
  private String profileImageUrl;

  private Long age;

  @Column(length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(length = 6)
  private Gender gender;

  @Getter
  public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    ETC("기타");

    private final String description;

    Gender(String description) {
      this.description = description;
    }

    public static Gender fromDescription(String description) {
      return java.util.Arrays.stream(values())
          .filter(gender -> gender.getDescription().equals(description))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("일치하는 성별이 없습니다: " + description));
    }
  }

  @Column(name = "phone_number", length = 13)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  public enum Role {
    USER,
    ADMIN
  }

  @Column(length = 50)
  private String position;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AuthProvider authProvider; // Local, Google, Kakao 등

  public enum AuthProvider {
    LOCAL,
    GOOGLE,
    KAKAO
  }

  public static Member create(
      String email,
      String passwordHash,
      String name,
      Long age,
      String address,
      Gender gender,
      String phoneNumber,
      Role role,
      String position,
      AuthProvider authProvider) {
    return Member.builder()
        .email(email)
        .passwordHash(passwordHash)
        .name(name)
        .age(age)
        .address(address)
        .gender(gender)
        .phoneNumber(phoneNumber)
        .role(role)
        .position(position)
        .createdAt(LocalDateTime.now())
        .authProvider(authProvider)
        .build();
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public void toggleDelete() {
    if (this.deletedAt == null) {
      this.deletedAt = LocalDateTime.now();
    } else {
      this.deletedAt = null;
    }
  }

  public void update(
      String passwordHash,
      String name,
      String profileImageUrl,
      Long age,
      String address,
      String phoneNumber,
      String position) {
    if (passwordHash != null) {
      this.passwordHash = passwordHash;
    }
    if (name != null) {
      this.name = name;
    }
    if (profileImageUrl != null) {
      this.profileImageUrl = profileImageUrl;
    }
    if (age != null) {
      this.age = age;
    }
    if (address != null) {
      this.address = address;
    }
    if (phoneNumber != null) {
      this.phoneNumber = phoneNumber;
    }
    if (position != null) {
      this.position = position;
    }
  }
}
