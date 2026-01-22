package com.edumax.molip.domain.course.entity;

import com.edumax.molip.domain.session.entity.Session;
import jakarta.persistence.*;
import lombok.*;

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
  private Session session;
}
