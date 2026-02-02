package com.edumax.molip.domain.course.service;

import com.edumax.molip.domain.course.dto.request.CourseCreateRequest;
import com.edumax.molip.domain.course.dto.request.CourseUpdateRequest;
import com.edumax.molip.domain.course.dto.request.ToggleZzimCourseRequest;
import com.edumax.molip.domain.course.dto.response.CourseDetailResponse;
import com.edumax.molip.domain.course.dto.response.GetCertificatedCourseResponse;
import com.edumax.molip.domain.course.dto.response.GetMyCertificationResponse;
import com.edumax.molip.domain.course.dto.response.GetTrainerCourseResponse;
import com.edumax.molip.domain.course.dto.response.GetZzimCourseResponse;
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

  List<GetZzimCourseResponse> getZzimCourses(Long workspaceId, Long memberId);

  ZzimCourseResponse toggleZzimCourse(
      Long workspaceId, ToggleZzimCourseRequest toggleZzimCourseRequest, Long memberId);

  List<GetCertificatedCourseResponse> getCertificatedCourses(Long workspaceId, Long memberId);

  List<GetTrainerCourseResponse> getTrainerCourses(Long workspaceId, Long memberId);

  List<GetMyCertificationResponse> getMyCertifications(Long memberId); // 일단 보류 (후순위)
}
