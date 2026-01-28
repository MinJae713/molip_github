package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseApply;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseApplyRepository extends JpaRepository<CourseApply, Long> {
  Optional<CourseApply> findByWorkspaceMemberAndCourse(
      WorkspaceMember workspaceMember, Course course);
}
