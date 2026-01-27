package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.CourseApply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseApplyRepository extends JpaRepository<CourseApply, Long> {
  boolean existsByCourse_CourseIdAndWorkspaceMember_Member_IdAndIsAppliedTrue(Long courseId, Long memberId);
}
