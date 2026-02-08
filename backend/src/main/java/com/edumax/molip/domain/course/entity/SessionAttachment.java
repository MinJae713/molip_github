package com.edumax.molip.domain.course.entity;

import com.edumax.molip.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "SessionAttachment")
@DiscriminatorValue("SESSION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionAttachment extends Attachment {

  @ManyToOne
  @JoinColumn(name = "session_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Session session;
}
