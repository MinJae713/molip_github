package com.edumax.molip.domain.chatting.entity;

import com.edumax.molip.domain.session.entity.Session;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "SessionChattingRoom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  SessionChattingRoom {

  @Id
  @Column(name = "session_chatting_room_id", length = 36)
  private String sessionChattingRoomId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @OneToOne
  @JoinColumn(name = "session_id", nullable = false, unique = true)
  private Session session;

  @OneToMany(mappedBy = "sessionChattingRoom")
  private List<SessionChattingMessage> messages;
}
