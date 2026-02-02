package com.edumax.molip.domain.course.repository;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseLearnerAnalysis;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseLearnerAnalysisRepository
    extends JpaRepository<CourseLearnerAnalysis, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE) // for update lock
  @Query(
      """
    SELECT cla FROM CourseLearnerAnalysis cla
    WHERE cla.course = :course AND cla.memberId IN :memberIdList
    """)
  List<CourseLearnerAnalysis> findAllByCourseAndMemberIdList(
      @Param("course") Course course, @Param("memberIdList") List<Long> memberIdList);

  @Query(
      """
    SELECT sum(cla.sumAttendance), sum(cla.sumConcentrate), sum(cla.sumQuizCorrect) FROM CourseLearnerAnalysis cla
    WHERE cla.course.id = :courseId
    """)
  Object[] findSumByCourseId(@Param("courseId") Long courseId);

  Long countByCourseCourseId(Long courseId);
}
