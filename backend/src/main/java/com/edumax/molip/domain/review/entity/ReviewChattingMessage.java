package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ReviewChattingMessage")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReviewChattingMessage {

  @Id
  @GeneratedValue
  @Column(name = "chatting_message_id", nullable = false)
  private Long chattingMessageId;

  @Column(nullable = false, length = 1000)
  private String message;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "room_id", nullable = false)
  private ReviewChattingRoom room;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id")
  private WorkspaceMember workspaceMember; // null이면 AI
}

