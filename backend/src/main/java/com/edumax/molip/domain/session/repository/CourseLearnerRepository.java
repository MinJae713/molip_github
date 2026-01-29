package com.edumax.molip.domain.session.repository;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.session.entity.CourseLearner;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("PMD.MethodNamingConventions")
public interface CourseLearnerRepository extends JpaRepository<CourseLearner, Long> {
  boolean existsByCourse_CourseIdAndWorkspaceMember_WorkspaceMemberId(
      Long courseId, Long workspaceMemberId);

  Optional<CourseLearner> findByCourse_CourseIdAndWorkspaceMember_WorkspaceMemberId(
      Long courseId, Long workspaceMemberId);

  List<CourseLearner> findByWorkspaceMember_Member_Id(Long memberId);

  Optional<CourseLearner> findByCourseAndWorkspaceMember(
      Course course, WorkspaceMember workspaceMember);
}
