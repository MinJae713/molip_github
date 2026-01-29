package com.edumax.molip.domain.course.service;

import com.edumax.molip.domain.course.dto.request.CourseCreateRequest;
import com.edumax.molip.domain.course.dto.request.CourseUpdateRequest;
import com.edumax.molip.domain.course.dto.response.CourseDetailResponse;
import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseApply;
import com.edumax.molip.domain.course.repository.CourseApplyRepository;
import com.edumax.molip.domain.course.repository.CourseRepository;
import com.edumax.molip.domain.course.types.CourseStatus;
import com.edumax.molip.domain.session.entity.CourseLearner;
import com.edumax.molip.domain.session.repository.CourseLearnerRepository;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import com.edumax.molip.domain.workspace.repository.WorkspaceMemberRepository;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final CourseApplyRepository courseApplyRepository;
  private final CourseLearnerRepository courseLearnerRepository;

  @Override
  @Transactional
  public Long createCourse(CourseCreateRequest request, Long memberId) {
    // 1. 워크스페이스 멤버 & Role 확인 (강사 여부 체크)
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMember_IdAndWorkspace_WorkspaceId(memberId, request.getWorkspaceId())
            .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스의 멤버가 아닙니다."));

    if (workspaceMember.getRole() != WorkspaceMemberRole.TRAINER) {
      throw new IllegalArgumentException("강사(TRAINER) 권한이 필요합니다.");
    }

    // 2. Course 엔티티 생성
    Course course =
        Course.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .subCategory(request.getSubCategory())
            .maxLearner(request.getMaxLearner())
            .isEngageCheck(request.getIsEngageCheck())
            .isSentimentAnalysis(request.getIsSentimentAnalysis())
            .attendance(request.getAttendance())
            .engagement(request.getEngagement()) // Nullable
            .exitQuiz(request.getExitQuiz()) // Nullable
            .status(CourseStatus.PREPARING) // 기본 상태 PREPARING으로 변경
            .period(request.getPeriod())
            .category(request.getCategory())
            // 연관관계 매핑
            .workspace(workspaceMember.getWorkspace())
            .leaner(workspaceMember) // 강사(Owner) 매핑 (변수명 오타는 엔티티 그대로 따름)
            .build();

    // 3. 저장
    Course savedCourse = courseRepository.save(course);

    return savedCourse.getCourseId();
  }

  @Override
  public CourseDetailResponse getCourseDetail(Long courseId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));

    return CourseDetailResponse.from(course);
  }

  @Override
  public java.util.List<CourseDetailResponse> getCourseListByWorkspace(Long workspaceId) {
    return courseRepository.findByWorkspace_WorkspaceId(workspaceId).stream()
        .map(CourseDetailResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public void updateCourse(Long courseId, CourseUpdateRequest request, Long memberId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));

    validateOwner(course, memberId);

    // Update fields
    if (request.getTitle() != null) {
      course.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      course.setDescription(request.getDescription());
    }
    if (request.getSubCategory() != null) {
      course.setSubCategory(request.getSubCategory());
    }
    if (request.getCategory() != null) {
      course.setCategory(request.getCategory());
    }
    if (request.getMaxLearner() != null) {
      course.setMaxLearner(request.getMaxLearner());
    }
    if (request.getPeriod() != null) {
      course.setPeriod(request.getPeriod());
    }
    if (request.getIsEngageCheck() != null) {
      course.setIsEngageCheck(request.getIsEngageCheck());
    }
    if (request.getIsSentimentAnalysis() != null) {
      course.setIsSentimentAnalysis(request.getIsSentimentAnalysis());
    }
    if (request.getAttendance() != null) {
      course.setAttendance(request.getAttendance());
    }
    if (request.getEngagement() != null) {
      course.setEngagement(request.getEngagement());
    }
    if (request.getExitQuiz() != null) {
      course.setExitQuiz(request.getExitQuiz());
    }
  }

  @Override
  @Transactional
  public void deleteCourse(Long courseId, Long memberId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));

    validateOwner(course, memberId);

    courseRepository.delete(course);
  }

  @Override
  @Transactional
  public void applyCourse(Long courseId, Long memberId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));

    // 1. 해당 워크스페이스의 멤버인지 확인
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMember_IdAndWorkspace_WorkspaceId(
                memberId, course.getWorkspace().getWorkspaceId())
            .orElseThrow(() -> new IllegalArgumentException("해당 워크스페이스의 멤버가 아닙니다."));

    // 2. 강사는 수강신청 불가 (LEARNER만 가능)
    if (workspaceMember.getRole() == WorkspaceMemberRole.TRAINER) {
      throw new IllegalArgumentException("강사는 수강 신청할 수 없습니다.");
    }

    // 3. 상태 체크 (ACTIVE)
    if (workspaceMember.getStatus() != WorkspaceMemberStatus.ACTIVE) {
      throw new IllegalArgumentException("활성 상태의 멤버만 신청 가능합니다.");
    }

    // 4. 이미 수강 중인지 확인
    if (courseLearnerRepository.existsByCourse_CourseIdAndWorkspaceMember_WorkspaceMemberId(
        courseId, workspaceMember.getWorkspaceMemberId())) {
      throw new IllegalArgumentException("이미 수강 중인 코스입니다.");
    }

    // 5. 이미 신청 중인지 확인
    if (courseApplyRepository
        .findByCourse_CourseIdAndWorkspaceMember_WorkspaceMemberId(
            courseId, workspaceMember.getWorkspaceMemberId())
        .isPresent()) {
      throw new IllegalArgumentException("이미 수강 신청된 코스입니다.");
    }

    // 6. 신청 생성 (isApplied = false)
    CourseApply courseApply =
        CourseApply.builder()
            .course(course)
            .workspaceMember(workspaceMember)
            .isApplied(false)
            .build();

    courseApplyRepository.save(courseApply);
  }

  @Override
  @Transactional
  public void approveCourseApplication(
      Long courseId, Long applicantWorkspaceMemberId, Long instructorMemberId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 코스를 찾을 수 없습니다."));

    // 1. 요청자가 강사(Owner)인지 확인
    validateOwner(course, instructorMemberId);

    // 2. 신청 내역 조회
    CourseApply courseApply =
        courseApplyRepository
            .findByCourse_CourseIdAndWorkspaceMember_WorkspaceMemberId(
                courseId, applicantWorkspaceMemberId)
            .orElseThrow(() -> new IllegalArgumentException("해당 수강 신청 내역이 없습니다."));

    // 3. 승인 처리 (isApplied = true)
    courseApply.setIsApplied(true); // Dirty Checking

    // 4. 수강생(CourseLearner) 등록
    CourseLearner courseLearner =
        CourseLearner.builder()
            .course(course)
            .workspaceMember(courseApply.getWorkspaceMember())
            .build();

    courseLearnerRepository.save(courseLearner);
  }

  @Override
  public java.util.List<CourseDetailResponse> getMyCourses(Long memberId) {
    // 1. 내가 만든 코스 (Owner)
    java.util.List<Course> ownedCourses = courseRepository.findByLeaner_Member_Id(memberId);

    // 2. 내가 수강 중인 코스 (Learner)
    java.util.List<Course> enrolledCourses =
        courseLearnerRepository.findByWorkspaceMember_Member_Id(memberId).stream()
            .map(CourseLearner::getCourse)
            .toList();

    // 3. 합치기 (중복 제거를 위해 Set 사용 가능하지만, 로직상 중복은 없을 것으로 예상됨 - 강사는 수강신청 불가)
    // 그래도 안전하게 Set으로 합치거나 List로 합침
    java.util.Set<Course> allCourses = new java.util.HashSet<>();
    allCourses.addAll(ownedCourses);
    allCourses.addAll(enrolledCourses);

    // 4. DTO 변환 (isOwner 계산 포함)
    return allCourses.stream()
        .map(course -> CourseDetailResponse.from(course, memberId))
        .sorted((c1, c2) -> c2.getCourseId().compareTo(c1.getCourseId())) // 최신순 정렬
        .toList();
  }

  private void validateOwner(Course course, Long memberId) {
    // 코스 소유자(강사)인지 확인
    // course.getLeaner()는 WorkspaceMember(Owner)
    if (!course.getLeaner().getMember().getId().equals(memberId)) {
      throw new IllegalArgumentException("코스 소유자만 수정/삭제할 수 있습니다.");
    }
  }
}
