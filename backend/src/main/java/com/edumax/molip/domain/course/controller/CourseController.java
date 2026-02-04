package com.edumax.molip.domain.course.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.course.dto.request.CourseCreateRequest;
import com.edumax.molip.domain.course.dto.request.CourseUpdateRequest;
import com.edumax.molip.domain.course.dto.request.ToggleZzimCourseRequest;
import com.edumax.molip.domain.course.dto.response.CourseDetailResponse;
import com.edumax.molip.domain.course.dto.response.GetCertificatedCourseResponse;
import com.edumax.molip.domain.course.dto.response.GetCourseApplyResponse;
import com.edumax.molip.domain.course.dto.response.GetCourseLearnerResponse;
import com.edumax.molip.domain.course.dto.response.GetMyCertificationResponse;
import com.edumax.molip.domain.course.dto.response.GetMyCourseStatsResponse;
import com.edumax.molip.domain.course.dto.response.GetTrainerCourseResponse;
import com.edumax.molip.domain.course.dto.response.GetZzimCourseResponse;
import com.edumax.molip.domain.course.dto.response.ZzimCourseResponse;
import com.edumax.molip.domain.course.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

  private final CourseService courseService;

  @PostMapping
  public ResponseEntity<Long> createCourse(
      @RequestBody CourseCreateRequest request,
      @AuthenticationPrincipal MemberUserDetails userDetails) {

    Long courseId = courseService.createCourse(request, userDetails.getMemberId());
    return ResponseEntity.ok(courseId);
  }

  @GetMapping("/{courseId}")
  public ResponseEntity<CourseDetailResponse> getCourseDetail(@PathVariable Long courseId) {
    return ResponseEntity.ok(courseService.getCourseDetail(courseId));
  }

  @GetMapping
  public ResponseEntity<List<CourseDetailResponse>> getCourseList(@RequestParam Long workspaceId) {
    return ResponseEntity.ok(courseService.getCourseListByWorkspace(workspaceId));
  }

  @PutMapping("/{courseId}")
  public ResponseEntity<String> updateCourse(
      @PathVariable Long courseId,
      @RequestBody CourseUpdateRequest request,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    try {
      courseService.updateCourse(courseId, request, userDetails.getMemberId());
      return ResponseEntity.ok("코스 수정 완료");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }
  }

  @DeleteMapping("/{courseId}")
  public ResponseEntity<String> deleteCourse(
      @PathVariable Long courseId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    try {
      courseService.deleteCourse(courseId, userDetails.getMemberId());
      return ResponseEntity.ok("코스 삭제 완료");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }
  }

  @PostMapping("/{courseId}/apply")
  public ResponseEntity<String> applyCourse(
      @PathVariable Long courseId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    try {
      courseService.applyCourse(courseId, userDetails.getMemberId());
      return ResponseEntity.ok("수강 신청 완료 (승인 대기)");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }
  }

  @PostMapping("/{courseId}/approve/{workspaceMemberId}")
  public ResponseEntity<String> approveCourseApplication(
      @PathVariable Long courseId,
      @PathVariable Long workspaceMemberId,
      @AuthenticationPrincipal MemberUserDetails userDetails) {
    try {
      courseService.approveCourseApplication(
          courseId, workspaceMemberId, userDetails.getMemberId());
      return ResponseEntity.ok("수강 승인 완료");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }
  }

  @GetMapping("/me")
  public ResponseEntity<List<CourseDetailResponse>> getMyCourses(
      @RequestParam Long workspaceId, @AuthenticationPrincipal MemberUserDetails userDetails) {
    return ResponseEntity.ok(courseService.getMyCourses(userDetails.getMemberId(), workspaceId));
  }

  @GetMapping("/{workspaceId}/zzim")
  public ResponseEntity<List<GetZzimCourseResponse>> getZzimCourses(
      @PathVariable Long workspaceId, @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(
        courseService.getZzimCourses(workspaceId, memberDetails.getMemberId()));
  }

  @PostMapping("/{workspaceId}/zzim")
  public ResponseEntity<ZzimCourseResponse> toggleZzimCourse(
      @PathVariable Long workspaceId,
      @Valid @RequestBody ToggleZzimCourseRequest toggleZzimCourseRequest,
      @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(
        courseService.toggleZzimCourse(
            workspaceId, toggleZzimCourseRequest, memberDetails.getMemberId()));
  }

  @GetMapping("/{workspaceId}/certificated")
  public ResponseEntity<List<GetCertificatedCourseResponse>> getCertificatedCourses(
      @PathVariable Long workspaceId, @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(
        courseService.getCertificatedCourses(workspaceId, memberDetails.getMemberId()));
  }

  @GetMapping("/{workspaceId}/trainer")
  public ResponseEntity<List<GetTrainerCourseResponse>> getTrainerCourses(
      @PathVariable Long workspaceId, @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(
        courseService.getTrainerCourses(workspaceId, memberDetails.getMemberId()));
  }

  @GetMapping("/me/certificated")
  public ResponseEntity<List<GetMyCertificationResponse>> getMyCertifications(
      @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(courseService.getMyCertifications(memberDetails.getMemberId()));
  }

  @GetMapping("/{courseId}/stats")
  public ResponseEntity<GetMyCourseStatsResponse> getMyCourseStats(
      @PathVariable Long courseId, @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(courseService.getMyCourseStats(courseId, memberDetails.getMemberId()));
  }

  @GetMapping("/{courseId}/applies")
  public ResponseEntity<List<GetCourseApplyResponse>> getCourseApplies(
      @PathVariable Long courseId, @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(
        courseService.getCourseAppliesResponse(courseId, memberDetails.getMemberId()));
  }

  @GetMapping("/{courseId}/learners")
  public ResponseEntity<List<GetCourseLearnerResponse>> getCourseLearners(
      @PathVariable Long courseId, @AuthenticationPrincipal MemberUserDetails memberDetails) {
    return ResponseEntity.ok(
        courseService.getCourseLearnersResponse(courseId, memberDetails.getMemberId()));
  }
}
