package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseCertificated;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseCertificatedRepository extends JpaRepository<CourseCertificated, Long> {
  Optional<CourseCertificated> findByWorkspaceMemberAndCourse(
      WorkspaceMember workspaceMember, Course course);
}
