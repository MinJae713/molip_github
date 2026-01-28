package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.CourseApply;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseApplyRepository extends JpaRepository<CourseApply, Long> {
  boolean existsByCourse_CourseIdAndWorkspaceMember_Member_IdAndIsAppliedTrue(Long courseId, Long memberId);

  @Query("SELECT COUNT(ca) > 0 FROM CourseApply ca WHERE ca.course.courseId = :courseId AND ca.workspaceMember.member.id = :memberId AND ca.isApplied = true")
  boolean existsByMemberIdAndCourseId(@Param("memberId") Long memberId, @Param("courseId") Long courseId);
   Optional<CourseApply> findByWorkspaceMemberAndCourse(WorkspaceMember workspaceMember, Course course);
}
