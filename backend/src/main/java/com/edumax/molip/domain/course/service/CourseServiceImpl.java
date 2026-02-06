package com.edumax.molip.domain.course.service;

import com.edumax.molip.domain.course.dto.request.ApproveCourseApplicationRequest;
import com.edumax.molip.domain.course.dto.request.CourseCreateRequest;
import com.edumax.molip.domain.course.dto.request.CourseUpdateRequest;
import com.edumax.molip.domain.course.dto.request.ToggleZzimCourseRequest;
import com.edumax.molip.domain.course.dto.response.ApproveCourseApplicationResponse;
import com.edumax.molip.domain.course.dto.response.CourseDetailResponse;
import com.edumax.molip.domain.course.dto.response.GetCertificatedCourseResponse;
import com.edumax.molip.domain.course.dto.response.GetCourseApplyResponse;
import com.edumax.molip.domain.course.dto.response.GetCourseLearnerResponse;
import com.edumax.molip.domain.course.dto.response.GetMyCertificationResponse;
import com.edumax.molip.domain.course.dto.response.GetTrainerCourseResponse;
import com.edumax.molip.domain.course.dto.response.GetZzimCourseResponse;
import com.edumax.molip.domain.course.dto.response.ZzimCourseResponse;
import com.edumax.molip.domain.course.dto.response.get_my_course_stats.GetMyCourseStatsResponse;
import com.edumax.molip.domain.course.dto.response.get_my_course_stats.GetMyCourseStatsResponseLearner;
import com.edumax.molip.domain.course.dto.response.get_my_course_stats.GetMyCourseStatsResponseLearner.SessionRecord;
import com.edumax.molip.domain.course.dto.response.get_my_course_stats.GetMyCourseStatsResponseTrainer;
import com.edumax.molip.domain.course.dto.response.get_my_course_stats.GetMyCourseStatsResponseTrainer.CourseLearnerAnalysisResponse;
import com.edumax.molip.domain.course.dto.response.get_my_course_stats.GetMyCourseStatsResponseTrainer.SessionAnalysisResponse;
import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.entity.CourseApply;
import com.edumax.molip.domain.course.entity.CourseCertificated;
import com.edumax.molip.domain.course.entity.CourseLearnerAnalysis;
import com.edumax.molip.domain.course.entity.Zzim;
import com.edumax.molip.domain.course.exception.AlreadyAppliedCourseApplyException;
import com.edumax.molip.domain.course.exception.IncorrectCourseTrainerException;
import com.edumax.molip.domain.course.exception.IncorrectWorkspaceCourseException;
import com.edumax.molip.domain.course.exception.NoCourseApplyException;
import com.edumax.molip.domain.course.exception.NoCourseException;
import com.edumax.molip.domain.course.exception.NoZzimException;
import com.edumax.molip.domain.course.repository.CourseApplyRepository;
import com.edumax.molip.domain.course.repository.CourseCertificatedRepository;
import com.edumax.molip.domain.course.repository.CourseRepository;
import com.edumax.molip.domain.course.repository.ZzimRepository;
import com.edumax.molip.domain.course.types.CourseStatus;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.member.exception.NoMemberException;
import com.edumax.molip.domain.member.repository.MemberRepository;
import com.edumax.molip.domain.review.entity.LearnerAnalysis;
import com.edumax.molip.domain.review.repository.LearnerAnalysisRepository;
import com.edumax.molip.domain.session.entity.CourseLearner;
import com.edumax.molip.domain.session.entity.CourseLearnerSession;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.session.exception.NoCourseLearnerException;
import com.edumax.molip.domain.session.repository.CourseLearnerRepository;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import com.edumax.molip.domain.workspace.exception.NoTrainerException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceMemberException;
import com.edumax.molip.domain.workspace.repository.WorkspaceMemberRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceRepository;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final CourseApplyRepository courseApplyRepository;
  private final CourseLearnerRepository courseLearnerRepository;
  private final CourseCertificatedRepository courseCertificatedRepository;
  private final ZzimRepository zzimRepository;

  private final WorkspaceRepository workspaceRepository;
  private final MemberRepository memberRepository;
  private final LearnerAnalysisRepository learnerAnalysisRepository;

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
  public List<CourseDetailResponse> getCourseListByWorkspace(Long workspaceId) {
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
  public ApproveCourseApplicationResponse approveCourseApplication(
      Long courseId,
      ApproveCourseApplicationRequest approveCourseApplicationRequest,
      Long instructorMemberId) {
    Course course = courseRepository.findById(courseId).orElseThrow(NoCourseException::new);

    // 1. 요청자가 강사(Owner)인지 확인
    validateOwner(course, instructorMemberId);
    Long workspaceMemberId = approveCourseApplicationRequest.getWorkspaceMemberId();
    Boolean isApply = approveCourseApplicationRequest.getIsApply();

    // 2. 신청 내역 조회
    CourseApply courseApply =
        courseApplyRepository
            .findByCourse_CourseIdAndWorkspaceMember_WorkspaceMemberId(courseId, workspaceMemberId)
            .orElseThrow(NoCourseApplyException::new);

    // 3. 승인/거절 처리
    if (courseApply.getIsApplied()) {
      throw new AlreadyAppliedCourseApplyException();
    }

    courseApply.setIsApplied(isApply); // Dirty Checking
    if (isApply) {
      // 4. 수강생(CourseLearner) 등록
      CourseLearner courseLearner =
          CourseLearner.builder()
              .course(course)
              .workspaceMember(courseApply.getWorkspaceMember())
              .build();
      courseLearnerRepository.save(courseLearner);
    }
    return ApproveCourseApplicationResponse.builder()
        .message(isApply ? "수강 승인 완료" : "수강 거절 완료")
        .build();
  }

  @Override
  public List<CourseDetailResponse> getMyCourses(Long memberId, Long workspaceId) {
    // 1. 내가 만든 코스 (Owner)
    List<Course> ownedCourses =
        courseRepository.findByLeaner_Member_IdAndWorkspace_WorkspaceId(memberId, workspaceId);

    // 2. 내가 수강 중인 코스 (Learner)
    List<Course> enrolledCourses =
        courseLearnerRepository
            .findByWorkspaceMember_Member_IdAndCourse_Workspace_WorkspaceId(memberId, workspaceId)
            .stream()
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

  @Override
  public List<GetZzimCourseResponse> getZzimCourses(Long workspaceId, Long memberId) {
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceMemberException::new);
    List<Zzim> zzims = workspaceMember.getZzims();
    return zzims.stream()
        .map(
            zzim -> {
              Long zzimId = zzim.getZzimId();
              Course course = zzim.getCourse();
              String title = course.getTitle();
              String description = course.getDescription();
              String subCategory = course.getSubCategory();
              List<Session> sessions = course.getSessions();
              long courseTime = 0L;
              for (Session session : sessions) {
                LocalTime startAt = session.getStartAt();
                LocalTime endAt = session.getEndAt();
                courseTime += ChronoUnit.HOURS.between(startAt, endAt);
              }
              Integer learnerCount = course.getCourseLearners().size();
              Boolean isApplied =
                  courseLearnerRepository
                      .findByCourseAndWorkspaceMember(course, workspaceMember)
                      .isPresent();

              return GetZzimCourseResponse.builder()
                  .zzimId(zzimId)
                  .title(title)
                  .description(description)
                  .subCategory(subCategory)
                  .courseTime(courseTime)
                  .learnerCount(learnerCount)
                  .isApplied(isApplied)
                  .build();
            })
        .toList();
  }

  @Override
  @Transactional
  public ZzimCourseResponse toggleZzimCourse(
      Long workspaceId, ToggleZzimCourseRequest toggleZzimCourseRequest, Long memberId) {
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceMemberException::new);
    Long courseId = toggleZzimCourseRequest.getCourseId();
    Course course = courseRepository.findById(courseId).orElseThrow(NoCourseException::new);
    if (!course.getWorkspace().getWorkspaceId().equals(workspaceId)) {
      throw new IncorrectWorkspaceCourseException();
    }
    if (toggleZzimCourseRequest.getIsZzim()) {
      zzimRepository.save(Zzim.builder().workspaceMember(workspaceMember).course(course).build());
    } else {
      Zzim zzim =
          zzimRepository
              .findByWorkspaceMemberAndCourse(workspaceMember, course)
              .orElseThrow(NoZzimException::new);
      zzimRepository.delete(zzim);
    }
    return ZzimCourseResponse.builder().apiResult(true).build();
  }

  @Override
  public List<GetCertificatedCourseResponse> getCertificatedCourses(
      Long workspaceId, Long memberId) {
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceMemberException::new);
    return workspaceMember.getCourseCertificateds().stream()
        .map(GetCertificatedCourseResponse::from)
        .toList();
  }

  @Override
  public List<GetTrainerCourseResponse> getTrainerCourses(Long workspaceId, Long memberId) {
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceMemberException::new);
    if (workspaceMember.getRole() != WorkspaceMemberRole.TRAINER) {
      throw new NoTrainerException();
    }
    List<Course> courses = workspaceMember.getCourses(); // 강사의 강의들
    return courses.stream().map(GetTrainerCourseResponse::from).toList();
  }

  @Override
  public List<GetMyCertificationResponse> getMyCertifications(Long memberId) {
    return List.of();
  }

  @Override
  public GetMyCourseStatsResponse getCourseStats(Long courseId, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    Course course = courseRepository.findById(courseId).orElseThrow(NoCourseException::new);
    Workspace workspace = course.getWorkspace();
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceException::new);
    return workspaceMember.getRole() == WorkspaceMemberRole.LEARNER
        ? getMyCourseStatsResponseLearner(course, workspaceMember)
        : getMyCourseStatsResponseTrainer(course, workspaceMember);
  }

  private GetMyCourseStatsResponseLearner getMyCourseStatsResponseLearner(
      Course course, WorkspaceMember workspaceMember) {
    CourseLearner courseLearner =
        courseLearnerRepository
            .findByCourseAndWorkspaceMember(course, workspaceMember)
            .orElseThrow(NoCourseLearnerException::new);

    Integer totalLearningTime = 0;
    int exitQuizAverage = 0;
    int concentrateMyAverage = 0;
    List<SessionRecord> sessionRecords = new ArrayList<>();
    List<CourseLearnerSession> courseLearnerSessions = courseLearner.getCourseLearnerSessions();
    int finishedSessionCount = 0;
    for (CourseLearnerSession courseLearnerSession : courseLearnerSessions) {
      Session session = courseLearnerSession.getSession();
      Long sessionId = session.getSessionId();
      Integer learningTime = courseLearnerSession.getAttendanceTime();
      totalLearningTime += learningTime;
      int concentrate = Math.round(courseLearnerSession.getAvgConcentrate());
      concentrateMyAverage += concentrate; // 세션별 몰입도
      Optional<LearnerAnalysis> learnerAnalysis =
          learnerAnalysisRepository.findById(session.getSessionId());

      int concentrateTotalAverage = 0;
      boolean isProceed = learnerAnalysis.isPresent();
      if (isProceed) {
        concentrateTotalAverage = Math.round(learnerAnalysis.orElse(null).getAvgConcentrate());
        finishedSessionCount++;
      }

      String title = session.getTitle();
      LocalDate date = session.getDate();
      Boolean isAttendance = courseLearnerSession.getIsAttendance();
      Integer exitQuiz = courseLearnerSession.getExitTicket();
      if (exitQuiz == null) {
        exitQuiz = 0;
      }
      exitQuizAverage += exitQuiz; // 세션별 퇴실 퀴즈 점수
      sessionRecords.add(
          SessionRecord.builder()
              .sessionId(sessionId)
              .learningTime(learningTime)
              .concentrate(concentrate)
              .concentrateTotalAverage(concentrateTotalAverage)
              .title(title)
              .date(date)
              .isAttendance(isAttendance)
              .exitQuiz(exitQuiz)
              .isProceed(isProceed)
              .build());
    }
    if (finishedSessionCount > 0) {
      exitQuizAverage /= finishedSessionCount; // 세션별 퇴실 퀴즈 평균
      concentrateMyAverage /= finishedSessionCount; // 세션별 몰입도 평균
    }
    return GetMyCourseStatsResponseLearner.builder()
        .courseId(course.getCourseId())
        .totalLearningTime(totalLearningTime)
        .exitQuizAverage(exitQuizAverage)
        .concentrateMyAverage(concentrateMyAverage)
        .sessionRecords(sessionRecords)
        .build();
  }

  private GetMyCourseStatsResponseTrainer getMyCourseStatsResponseTrainer(
      Course course, WorkspaceMember workspaceMember) {
    Float avgAttandance = course.getAvgAttendance();
    Float avgConcentrate = course.getAvgConcentrate();
    Float avgQuizCorrect = course.getAvgQuizCorrect();

    List<Session> sessions = course.getSessions();
    List<SessionAnalysisResponse> sessionAnalysis =
        sessions.stream()
            .map(session -> learnerAnalysisRepository.findById(session.getSessionId()))
            .filter(Optional::isPresent)
            .map(learnerAnalysis -> learnerAnalysis.orElse(null))
            .map(SessionAnalysisResponse::from)
            .toList();

    List<CourseLearnerAnalysis> courseLearnerAnalyses = course.getLearnerAnalysis();
    Workspace workspace = workspaceMember.getWorkspace();
    List<CourseLearnerAnalysisResponse> courseLearnerAnalysisResponse =
        courseLearnerAnalyses.stream()
            .map(
                courseLearnerAnalysis -> {
                  Long memberId = courseLearnerAnalysis.getMemberId();
                  Member member =
                      memberRepository.findById(memberId).orElseThrow(NoMemberException::new);
                  WorkspaceMember targetWorkspaceMember =
                      workspaceMemberRepository
                          .findByMemberAndWorkspace(member, workspace)
                          .orElseThrow(NoWorkspaceMemberException::new);
                  Optional<CourseCertificated> courseCertificated =
                      courseCertificatedRepository.findByWorkspaceMemberAndCourse(
                          targetWorkspaceMember, course);
                  return CourseLearnerAnalysisResponse.from(
                      member,
                      courseLearnerAnalysis,
                      targetWorkspaceMember.getWorkspaceMemberId(),
                      courseCertificated.isPresent());
                })
            .toList();

    return GetMyCourseStatsResponseTrainer.builder()
        .isCertificated(course.isFinished())
        .courseId(course.getCourseId())
        .avgAttandance(avgAttandance)
        .avgConcentrate(avgConcentrate)
        .avgQuizCorrect(avgQuizCorrect)
        .sessionAnalysis(sessionAnalysis)
        .courseLearnerAnalysis(courseLearnerAnalysisResponse)
        .build();
  }

  @Override
  public List<GetCourseApplyResponse> getCourseAppliesResponse(Long courseId, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    Course course = courseRepository.findById(courseId).orElseThrow(NoCourseException::new);
    Workspace workspace = course.getWorkspace();
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceMemberException::new);
    if (workspaceMember.getRole() != WorkspaceMemberRole.TRAINER) {
      throw new NoTrainerException();
    }
    if (!course.getLeaner().getWorkspaceMemberId().equals(workspaceMember.getWorkspaceMemberId())) {
      throw new IncorrectCourseTrainerException();
    }

    List<CourseApply> courseApplies = course.getCourseApplies();
    return courseApplies.stream()
        .filter(courseApply -> !courseApply.getIsApplied())
        .map(
            courseApply -> {
              WorkspaceMember applyWorkspaceMember = courseApply.getWorkspaceMember();
              Member applyMember = applyWorkspaceMember.getMember();
              Long workspaceMemberId = applyWorkspaceMember.getWorkspaceMemberId();
              String email = applyMember.getEmail();
              Long courseApplyId = courseApply.getCourseApplyId();
              String profileImageUrl = applyMember.getProfileImageUrl();
              String name = applyMember.getName();
              String position = applyMember.getPosition();
              LocalDateTime createdAt = courseApply.getCreatedAt();
              return GetCourseApplyResponse.builder()
                  .memberId(applyMember.getId())
                  .workspaceMemberId(workspaceMemberId)
                  .email(email)
                  .courseApplyId(courseApplyId)
                  .profileImageUrl(profileImageUrl)
                  .name(name)
                  .position(position)
                  .createdAt(createdAt)
                  .build();
            })
        .toList();
  }

  @Override
  public List<GetCourseLearnerResponse> getCourseLearnersResponse(Long courseId, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    Course course = courseRepository.findById(courseId).orElseThrow(NoCourseException::new);
    Workspace workspace = course.getWorkspace();
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceMemberException::new);
    if (workspaceMember.getRole() != WorkspaceMemberRole.TRAINER) {
      throw new NoTrainerException();
    }
    if (!course.getLeaner().getWorkspaceMemberId().equals(workspaceMember.getWorkspaceMemberId())) {
      throw new IncorrectCourseTrainerException();
    }

    List<CourseLearner> courseLearners = course.getCourseLearners();
    return courseLearners.stream()
        .map(
            courseLearner -> {
              WorkspaceMember learnerWorkspaceMember = courseLearner.getWorkspaceMember();
              Member learnerMember = learnerWorkspaceMember.getMember();
              Long workspaceMemberId = learnerWorkspaceMember.getWorkspaceMemberId();
              String email = learnerMember.getEmail();
              Long courseLearnerId = courseLearner.getCourseLearnerId();
              String profileImageUrl = learnerMember.getProfileImageUrl();
              String name = learnerMember.getName();
              String position = learnerMember.getPosition();
              Double attandanceRate = courseLearner.getSessionAttandanceRate();
              return GetCourseLearnerResponse.builder()
                  .memberId(learnerMember.getId())
                  .workspaceMemberId(workspaceMemberId)
                  .email(email)
                  .courseLearnerId(courseLearnerId)
                  .profileImageUrl(profileImageUrl)
                  .name(name)
                  .position(position)
                  .attandanceRate(attandanceRate)
                  .build();
            })
        .toList();
  }
}
