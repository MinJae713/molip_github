package com.edumax.molip.domain.chatting.entity;

import com.edumax.molip.domain.review.entity.LearnerAnalysis;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(
    name = "review_chatting_room",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"session_id", "member_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewChattingRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "room_id", nullable = false)
  private Long roomId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private LearnerAnalysis learnerAnalysis;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workspace_member_id", nullable = true)
  @Builder.Default
  private WorkspaceMember workspaceMember = null;

  @Column(name = "member_id", nullable = true)
  @Builder.Default
  private Long memberId = null;

  /** 복습 채팅 메시지 */
  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ReviewChattingMessage> reviewChattingMessages = new ArrayList<>();

  public void addMessage(ReviewChattingMessage message) {
    this.reviewChattingMessages.add(message);
    message.setRoom(this);
  }
}
