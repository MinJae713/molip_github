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
  private Course course;
}
