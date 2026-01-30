package com.edumax.molip.domain.course.service;

import com.edumax.molip.domain.course.dto.request.CourseCreateRequest;
import com.edumax.molip.domain.course.dto.request.CourseUpdateRequest;
import com.edumax.molip.domain.course.dto.response.CourseDetailResponse;
import com.edumax.molip.domain.course.dto.response.GetCertificatedCoursesResponse;
import com.edumax.molip.domain.course.dto.response.GetZzimCoursesResponse;
import com.edumax.molip.domain.course.dto.response.ZzimCourseResponse;
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

  List<CourseDetailResponse> getMyCourses(Long memberId, Long workspaceId);

  GetZzimCoursesResponse getZzimCourses(Long workspaceId, Long memberId);

  ZzimCourseResponse zzimCourse(Long workspaceId, Long memberId, Long courseId);

  GetCertificatedCoursesResponse getCertificatedCourses(Long workspaceId, Long memberId);
}
