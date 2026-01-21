package com.edumax.molip.domain.session.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "QnARoom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnARoom {

  @Id
  @GeneratedValue
  @Column(name = "qna_room_id", length = 36)
  private String id;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToOne(optional = false)
  @JoinColumn(name = "session_id")
  private Session session;

  @OneToMany(
      mappedBy = "qnaRoom",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<QnAMessage> messages;
}

