package com.edumax.molip.domain.course.service;

import com.edumax.molip.domain.course.dto.request.CourseCreateRequest;
import com.edumax.molip.domain.course.dto.request.CourseUpdateRequest;
import com.edumax.molip.domain.course.dto.response.CourseDetailResponse;
import java.util.List;

public interface CourseService {
  Long createCourse(CourseCreateRequest request, Long memberId);

  CourseDetailResponse getCourseDetail(Long courseId);

  List<CourseDetailResponse> getCourseListByWorkspace(Long workspaceId);

  void updateCourse(Long courseId, CourseUpdateRequest request, Long memberId);

  void deleteCourse(Long courseId, Long memberId);

  void applyCourse(Long courseId, Long memberId);

  void approveCourseApplication(
      Long courseId, Long applicantWorkspaceMemberId, Long instructorMemberId);

  List<CourseDetailResponse> getMyCourses(Long memberId);
}
