package com.edumax.molip.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CourseCategory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCategory {

  @Id
  @GeneratedValue
  @Column(name = "course_category_id", nullable = false)
  private Long courseCategoryId;

  @Column(name = "course_category_name", nullable = false, length = 50)
  private String courseCategoryName;
}
