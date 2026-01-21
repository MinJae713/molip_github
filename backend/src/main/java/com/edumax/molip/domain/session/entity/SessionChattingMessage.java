package com.edumax.molip.domain.session.entity;

import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "SessionChattingMessage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionChattingMessage {

  @Id
  @GeneratedValue
  @Column(name = "session_chatting_message_id", nullable = false)
  private Long id;

  @Column(nullable = false, length = 1000)
  private String message;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "workspace_member_id", nullable = false)
  private WorkspaceMember workspaceMember;

  @ManyToOne(optional = false)
  @JoinColumn(name = "session_chatting_room_id")
  private SessionChattingRoom sessionChattingRoom;
}
