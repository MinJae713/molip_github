package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ReviewChattingRoom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewChattingRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "room_id", nullable = false)
  private String roomId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;

  /** 복습 채팅 메시지 */
  @OneToMany(mappedBy = "room")
  private List<ReviewChattingMessage> reviewChattingMessages;
}
