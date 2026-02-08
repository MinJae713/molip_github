package com.edumax.molip.domain.chatting.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "review_chatting_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewChattingMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "chatting_message_id", nullable = false)
  private Long chattingMessageId;

  @Column(nullable = false, length = 1000)
  private String message;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ReviewChattingRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_member_id", nullable = true)
  @Builder.Default
  private WorkspaceMember workspaceMember = null;

  @Column(name = "sender_id", nullable = true)
  @Builder.Default
  private Long senderId = null;
}
