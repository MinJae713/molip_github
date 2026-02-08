package com.edumax.molip.domain.course.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "CommonAttachment")
@DiscriminatorValue("SESSION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonAttachment extends Attachment {

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Course course;
}
