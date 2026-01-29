package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.repository.CourseApplyRepository;
import com.edumax.molip.domain.course.repository.ZzimRepository;
import com.edumax.molip.domain.course.types.CourseCategory;
import com.edumax.molip.domain.course.types.CourseStatus;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.member.exception.NoAdminException;
import com.edumax.molip.domain.member.exception.NoMemberException;
import com.edumax.molip.domain.member.repository.MemberRepository;
import com.edumax.molip.domain.review.repository.LearnerAnalysisRepository;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.dto.request.ChangeWorkspaceMemberRoleRequest;
import com.edumax.molip.domain.workspace.dto.request.CreateJoinCodeRequest;
import com.edumax.molip.domain.workspace.dto.request.CreateWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.request.InactiveWorkspaceMemberRequest;
import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.request.UpdateWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.ChangeWorkspaceMemberRoleResponse;
import com.edumax.molip.domain.workspace.dto.response.CreateJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.CreateWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.DeleteJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.DeleteWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.FindJoinCodeResponse;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceMemberByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceRegisterResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.InactiveWorkspaceMemberResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.UpdateWorkspaceResponse;
import com.edumax.molip.domain.workspace.entity.InviteCode;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import com.edumax.molip.domain.workspace.entity.WorkspaceRegister;
import com.edumax.molip.domain.workspace.exception.InviteCodeExistException;
import com.edumax.molip.domain.workspace.exception.NoInviteCodeException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceAppliedAtException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceMemberException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceMemberRoleException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceRegisterException;
import com.edumax.molip.domain.workspace.exception.WorkspaceMemberExistException;
import com.edumax.molip.domain.workspace.exception.WorkspaceRegisterExistException;
import com.edumax.molip.domain.workspace.repository.InviteCodeRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceMemberRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceRegisterRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceRepository;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberStatus;
import com.edumax.molip.domain.workspace.types.WorkspaceRegisterStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
  private final InviteCodeRepository inviteCodeRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final WorkspaceRegisterRepository workspaceRegisterRepository;
  private final WorkspaceRepository workspaceRepository;

  private final MemberRepository memberRepository;
  private final LearnerAnalysisRepository learnerAnalysisRepository;
  private final ZzimRepository zzimRepository;
  private final CourseApplyRepository courseApplyRepository;

  @Override
  public List<FindWorkspaceResponse> findWorkspaces(Long memberId) {
    List<Workspace> workspaces = workspaceRepository.findAll();
    return workspaces.stream().map(workspace -> findWorkspace(workspace, memberId)).toList();
  }

  @Override
  public FindWorkspaceResponse findWorkspaceById(Long workspaceId, Long memberId) {
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler
    return findWorkspace(workspace, memberId);
  }

  private FindWorkspaceResponse findWorkspace(Workspace workspace, Long memberId) {
    // 이 API를 관리자 쪽에서도 부르는가? - 그럼 로직 좀 바뀝니다
    Member member = memberRepository.findById(memberId).orElseThrow(NoMemberException::new);
    Long workspaceId = workspace.getWorkspaceId();
    String name = workspace.getName();
    String description = workspace.getDescription();
    String logoUrl = workspace.getLogoUrl();
    List<WorkspaceMember> workspaceMembers = workspace.getWorkspaceMembers();
    List<Course> courses = workspace.getCourses();

    WorkspaceRegister workspaceRegister =
        workspaceRegisterRepository.findByMemberAndWorkspace(member, workspace).orElse(null);
    String myStatus = "미신청";
    WorkspaceMemberRole myRole = null;
    /**
     * - workspaceRegister가 없으면 아얘 신청한 이력이 없는 것으로 간주합니다!! - 워크스페이스에 들어갔다고 workspaceRegister 테이블에서
     * 삭제되지 않음 - PENDING, REJECTED면 null (WorkspaceMember가 아니기 때문임)
     */
    if (workspaceRegister != null) {
      WorkspaceRegisterStatus status = workspaceRegister.getStatus();
      myStatus = status.getLabel(); // 가입 대기, 가입 완료, 반려됨
      if (status == WorkspaceRegisterStatus.APPROVE) {
        WorkspaceMember workspaceMember =
            workspaceMemberRepository
                .findByMemberAndWorkspace(member, workspace)
                .orElseThrow(NoWorkspaceMemberException::new);
        myRole = workspaceMember.getRole();
      }
    }

    return FindWorkspaceResponse.builder()
        .workspaceId(workspaceId)
        .name(name)
        .logoUrl(logoUrl)
        .description(description)
        .memberCount(workspaceMembers.size())
        .courseCount(courses.size())
        .myStatus(myStatus)
        .myRole(myRole)
        .build();
  }

  @Override
  @Transactional
  public JoinWorkspaceResponse joinWorkspace(JoinWorkspaceRequest joinWorkspaceRequest) {
    Long workspaceId = joinWorkspaceRequest.getWorkspaceId();
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler
    WorkspaceMemberRole role = joinWorkspaceRequest.getRole();
    Long memberId = joinWorkspaceRequest.getMemberId();
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    WorkspaceRegisterStatus status = joinWorkspaceRequest.getStatus();
    // PENDING, APPROVE, REJECTED
    LocalDateTime appliedAt = joinWorkspaceRequest.getAppliedAt();

    switch (status) {
      case PENDING:
        {
          // 사용자 워크스페이스 가입 신청
          // 워크 스페이스 가입 대기에 추가 (PENDING)
          if (workspaceRegisterRepository.findByMemberAndWorkspace(member, workspace).isPresent()) {
            throw new WorkspaceRegisterExistException();
          }
          workspaceRegisterRepository.save(
              WorkspaceRegister.builder()
                  .status(WorkspaceRegisterStatus.PENDING)
                  .member(member)
                  .workspace(workspace)
                  .build());
          break;
        }
      case APPROVE:
        {
          // 관리자 워크스페이스 가입 승인
          if (role == null) {
            throw new NoWorkspaceMemberRoleException();
          } else if (appliedAt == null) {
            throw new NoWorkspaceAppliedAtException();
          }
          WorkspaceRegister workspaceRegister =
              workspaceRegisterRepository
                  .findByMemberAndWorkspace(member, workspace)
                  .orElseThrow(NoWorkspaceRegisterException::new);
          // 워크 스페이스 가입 대기 수정 (APPROVE)
          workspaceRegister.setApprovedAt(appliedAt);
          workspaceRegister.setStatus(WorkspaceRegisterStatus.APPROVE);
          workspaceRegisterRepository.save(workspaceRegister);
          // 워크 스페이스 참여자에 추가
          if (workspaceMemberRepository.findByMemberAndWorkspace(member, workspace).isPresent()) {
            throw new WorkspaceMemberExistException();
          }
          workspaceMemberRepository.save(
              WorkspaceMember.builder()
                  .status(WorkspaceMemberStatus.ACTIVE)
                  .role(role)
                  .member(member)
                  .workspace(workspace)
                  .build());
          break;
        }
      case REJECTED:
        {
          // 관리자 워크스페이스 가입 반려
          WorkspaceRegister workspaceRegister =
              workspaceRegisterRepository
                  .findByMemberAndWorkspace(member, workspace)
                  .orElseThrow(NoWorkspaceRegisterException::new);
          // 워크 스페이스 가입 대기에서 삭제
          workspaceRegisterRepository.delete(workspaceRegister);
          // 워크 스페이스 참여자에서 삭제
          Optional<WorkspaceMember> workspaceMember =
              workspaceMemberRepository.findByMemberAndWorkspace(member, workspace);
          if (workspaceMember.isPresent()) {
            workspaceMemberRepository.delete(workspaceMember.orElse(null));
          }
          break;
        }
      default:
        {
        }
    }
    return JoinWorkspaceResponse.builder().apiResult(true).build();
  }

  @Override
  public FindMyStatsResponse findMyStats(Long workspaceId) { // 모르것다..?
    workspaceRepository
        .findById(workspaceId)
        .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler

    return null;
  }

  @Override
  public List<FindWorkspaceCoursesByIdResponse> findWorkspaceCoursesById(
      Long workspaceId, Long memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow(NoMemberException::new);
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findByMemberAndWorkspace(member, workspace)
            .orElseThrow(NoWorkspaceMemberException::new);
    // 위에서 예외 나오는 경우? -> 현재 사용자가 입력 받은 워스 멤버가 아니라면 에러남?
    // 프론트에서 그런 경우가 있나?
    List<Course> courses = workspace.getCourses();
    List<FindWorkspaceCoursesByIdResponse> result = new ArrayList<>();
    courses.forEach(
        course -> {
          Long courseId = course.getCourseId();
          String title = course.getTitle();
          String description = course.getDescription();
          CourseCategory category = course.getCategory();
          String subCategory = course.getSubCategory();
          List<Session> sessions = course.getSessions();
          Integer totalLessons = sessions.size();
          Integer currentLessons = 0;
          for (Session session : sessions) {
            currentLessons +=
                learnerAnalysisRepository.findById(session.getSessionId()).isPresent() ? 1 : 0;
          }
          Boolean isLiked =
              zzimRepository.findByWorkspaceMemberAndCourse(workspaceMember, course).isPresent();
          System.out.println(
              courseId + ", " + isLiked + ", " + workspaceMember.getWorkspaceMemberId());
          LocalDateTime dDay = course.getCreatedAt();
          CourseStatus status = course.getStatus();
          Boolean enrollmentStatus =
              courseApplyRepository
                  .findByWorkspaceMemberAndCourse(workspaceMember, course)
                  .isPresent();
          result.add(
              FindWorkspaceCoursesByIdResponse.builder()
                  .courseId(courseId)
                  .workspaceId(workspaceId)
                  .title(title)
                  .description(description)
                  .category(category)
                  .subCategory(subCategory) // null도 가능
                  .currentLessons(currentLessons)
                  .totalLessons(totalLessons)
                  .isLiked(isLiked)
                  .dDay(dDay)
                  .status(status)
                  .enrollmentStatus(enrollmentStatus)
                  .build());
        });
    return result;
  }

  @Override
  public List<FindWorkspaceResponse> findMyWorkspaces(Long memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow(NoMemberException::new);
    List<WorkspaceMember> workspaceMembers = workspaceMemberRepository.findByMember(member);
    return workspaceMembers.stream()
        .sorted(
            new Comparator<WorkspaceMember>() {
              @Override
              public int compare(WorkspaceMember o1, WorkspaceMember o2) {
                return o2.getWorkspaceMemberId().compareTo(o1.getWorkspaceMemberId());
              }
            })
        .map(workspaceMember -> findWorkspace(workspaceMember.getWorkspace(), memberId))
        .toList();
  }

  @Override
  public FindWorkspaceResponse searchWorkspaces(String searchQuery, Long memberId) {
    // 워스 가입 코드가 일치한 것으로 검색
    Optional<InviteCode> inviteCode = inviteCodeRepository.findByIdentifyValue(searchQuery);
    return inviteCode.isPresent()
        ? findWorkspace(inviteCode.orElse(null).getWorkspace(), memberId)
        : null;
  }

  @Override
  @Transactional
  public CreateWorkspaceResponse createWorkspace(
      CreateWorkspaceRequest createWorkspaceRequest, Long memberId) {
    String name = createWorkspaceRequest.getName();
    String description = createWorkspaceRequest.getDescription();

    Member member = memberRepository.findById(memberId).orElseThrow(NoMemberException::new);
    if (member.getRole() != Member.Role.ADMIN) {
      throw new NoAdminException();
    }

    workspaceRepository.save(
        Workspace.builder()
            .name(name)
            .description(description)
            .logoUrl("더미 - S3 연동 이후 URL이 들어감")
            .isSearchable(true)
            .member(member)
            .build());
    return CreateWorkspaceResponse.builder().apiResult(true).build();
  }

  @Override
  @Transactional
  public UpdateWorkspaceResponse updateWorkspace(UpdateWorkspaceRequest updateWorkspaceRequest) {
    Long workspaceId = updateWorkspaceRequest.getWorkspaceId();
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    String name = updateWorkspaceRequest.getName();
    if (name != null && !name.isEmpty()) {
      workspace.setName(name);
    }
    String description = updateWorkspaceRequest.getDescription();
    if (description != null && !description.isEmpty()) {
      workspace.setDescription(description);
    }
    //    String logoImage = updateWorkspaceRequest.getLogo_image(); // 임시
    //    if (logoImage != null && !logoImage.isEmpty())
    //      workspace.setLogoUrl(logoImage);
    // 임시
    workspaceRepository.save(workspace);
    return UpdateWorkspaceResponse.builder().apiResult(true).build();
  }

  @Override
  @Transactional
  public DeleteWorkspaceResponse deleteWorkspace(Long workspaceId, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    if (member.getRole() != Member.Role.ADMIN) {
      throw new NoAdminException();
    }
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    workspaceRepository.delete(workspace);
    return DeleteWorkspaceResponse.builder().apiResult(true).build();
  }

  @Override
  @Transactional
  public InactiveWorkspaceMemberResponse inactiveWorkspaceMember(
      InactiveWorkspaceMemberRequest inactiveWorkspaceMemberRequest, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    if (member.getRole() != Member.Role.ADMIN) {
      throw new NoAdminException();
    }
    Long workspaceMemberId = inactiveWorkspaceMemberRequest.getWorkspaceMemberId();
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findById(workspaceMemberId)
            .orElseThrow(NoWorkspaceMemberException::new);
    workspaceMember.setStatus(WorkspaceMemberStatus.INACTIVE);
    workspaceMemberRepository.save(workspaceMember);
    return InactiveWorkspaceMemberResponse.builder().apiResult(true).build();
  }

  @Override
  @Transactional
  public ChangeWorkspaceMemberRoleResponse changeWorkspaceMemberRole(
      ChangeWorkspaceMemberRoleRequest changeWorkspaceMemberRoleRequest, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    if (member.getRole() != Member.Role.ADMIN) {
      throw new NoAdminException();
    }
    Long workspaceMemberId = changeWorkspaceMemberRoleRequest.getWorkspaceMemberId();
    WorkspaceMember workspaceMember =
        workspaceMemberRepository
            .findById(workspaceMemberId)
            .orElseThrow(NoWorkspaceMemberException::new);
    WorkspaceMemberRole workspaceMemberRole =
        changeWorkspaceMemberRoleRequest.getWorkspaceMemberRole();
    workspaceMember.setRole(workspaceMemberRole);
    workspaceMemberRepository.save(workspaceMember);
    return ChangeWorkspaceMemberRoleResponse.builder().apiResult(true).build();
  }

  @Override
  @Transactional
  public CreateJoinCodeResponse createJoinCode(
      CreateJoinCodeRequest createJoinCodeRequest, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    if (member.getRole() != Member.Role.ADMIN) {
      throw new NoAdminException();
    }
    Long workspaceId = createJoinCodeRequest.getWorkspaceId();
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    String joinCodeName = createJoinCodeRequest.getJoinCodeName();
    Optional<InviteCode> inviteCode =
        inviteCodeRepository.findByCodeNameAndWorkspace(joinCodeName, workspace);
    if (inviteCode.isPresent()) {
      throw new InviteCodeExistException();
    }
    LocalDateTime endedDate = createJoinCodeRequest.getEndedDate();
    String identifyValue = RandomStringUtils.randomAlphanumeric(50);
    inviteCodeRepository.save(
        InviteCode.builder()
            .expiredAt(endedDate)
            .codeName(joinCodeName)
            .identifyValue(identifyValue)
            .workspace(workspace)
            .build());
    return CreateJoinCodeResponse.builder().identifyValue(identifyValue).build();
  }

  @Override
  public List<FindJoinCodeResponse> findJoinCodes(Long workspaceId, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    if (member.getRole() != Member.Role.ADMIN) {
      throw new NoAdminException();
    }
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    List<InviteCode> inviteCodes = workspace.getInviteCodes();
    return inviteCodes.stream().map(FindJoinCodeResponse::fromInviteCode).toList();
  }

  @Override
  @Transactional
  public DeleteJoinCodeResponse deleteJoinCode(Long joinCodeId, Long memberId) {
    Member member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(NoMemberException::new);
    if (member.getRole() != Member.Role.ADMIN) {
      throw new NoAdminException();
    }
    InviteCode inviteCode =
        inviteCodeRepository.findById(joinCodeId).orElseThrow(NoInviteCodeException::new);
    inviteCodeRepository.delete(inviteCode);
    return DeleteJoinCodeResponse.builder().apiResult(true).build();
  }

  @Override
  public List<FindWorkspaceMemberResponse> findWorkspaceMembers(Long workspaceId) {
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    List<WorkspaceMember> workspaceMembers = workspace.getWorkspaceMembers();
    return workspaceMembers.stream().map(FindWorkspaceMemberResponse::from).toList();
  }

  @Override
  public FindWorkspaceMemberByIdResponse findWorkspaceMemberById(
      Long workspaceId, Long workspaceMemberId) {
    // 얘는 나머지 테스트 이후 개발 예정
    //    Workspace workspace =
    //        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);

    return null;
  }

  @Override
  public List<FindWorkspaceRegisterResponse> findWorkspaceRegisters(Long workspaceId) {
    Workspace workspace =
        workspaceRepository.findById(workspaceId).orElseThrow(NoWorkspaceException::new);
    List<WorkspaceRegister> workspaceRegisters = workspace.getWorkspaceRegisters();
    return workspaceRegisters.stream().map(FindWorkspaceRegisterResponse::from).toList();
  }
}
