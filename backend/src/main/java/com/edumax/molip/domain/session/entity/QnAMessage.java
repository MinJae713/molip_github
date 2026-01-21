package com.edumax.molip.domain.session.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "QnAMessage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnAMessage {

  @Id
  @GeneratedValue
  @Column(name = "qna_message_id", nullable = false)
  private Long id;

  @Column(nullable = false, length = 10)
  private String type;

  @Column(nullable = false, length = 1000)
  private String message;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "qna_room_id")
  private QnARoom qnaRoom;

  @Column(name = "workspace_member_id", nullable = false)
  private Long workspaceMemberId;
}

