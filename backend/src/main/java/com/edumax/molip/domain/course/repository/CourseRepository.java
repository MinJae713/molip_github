package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("PMD.MethodNamingConventions")
public interface CourseRepository extends JpaRepository<Course, Long> {
  List<Course> findByWorkspace_WorkspaceId(Long workspaceId);

  List<Course> findByLeaner_Member_Id(Long memberId);

  List<Course> findByLeaner_Member_IdAndWorkspace_WorkspaceId(Long memberId, Long workspaceId);
}
