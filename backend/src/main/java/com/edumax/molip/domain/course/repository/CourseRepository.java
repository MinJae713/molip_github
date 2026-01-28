package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {}
