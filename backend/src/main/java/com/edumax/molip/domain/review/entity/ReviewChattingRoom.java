package com.edumax.molip.domain.review.entity;

import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ReviewChattingRoom")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReviewChattingRoom {

  @Id
  @GeneratedValue
  @Column(name = "room_id", nullable = false)
  private String roomId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;
}

