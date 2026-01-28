package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.course.repository.CourseApplyRepository;
import com.edumax.molip.domain.course.repository.ZzimRepository;
import com.edumax.molip.domain.course.types.CourseCategory;
import com.edumax.molip.domain.course.types.CourseStatus;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.member.exception.NoMemberException;
import com.edumax.molip.domain.member.repository.MemberRepository;
import com.edumax.molip.domain.review.repository.LearnerAnalysisRepository;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import com.edumax.molip.domain.workspace.entity.WorkspaceRegister;
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
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    List<Workspace> workspaceList = workspaceRepository.findAll();
    List<FindWorkspaceResponse> result = new ArrayList<>();
    workspaceList.forEach(workspace -> result.add(findWorkspace(workspace, memberId)));
    return result;
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
  public JoinWorkspaceResponse joinWorkspace(JoinWorkspaceRequest joinWorkspaceRequest) {
    Long workspaceId = joinWorkspaceRequest.getWorkspaceId();
    Workspace workspace =
        workspaceRepository
            .findById(workspaceId)
            .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler
    WorkspaceMemberRole role = joinWorkspaceRequest.getRole();
    Long memberId = joinWorkspaceRequest.getMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(NoMemberException::new); // GlobalExceptionHandler
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
}
