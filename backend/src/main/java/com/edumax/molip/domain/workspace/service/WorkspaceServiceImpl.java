package com.edumax.molip.domain.workspace.service;

import com.edumax.molip.domain.course.entity.Course;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.member.exception.NoMemberException;
import com.edumax.molip.domain.member.repository.MemberRepository;
import com.edumax.molip.domain.workspace.dto.request.JoinWorkspaceRequest;
import com.edumax.molip.domain.workspace.dto.response.FindMyStatsResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceCoursesByIdResponse;
import com.edumax.molip.domain.workspace.dto.response.FindWorkspaceResponse;
import com.edumax.molip.domain.workspace.dto.response.JoinWorkspaceResponse;
import com.edumax.molip.domain.workspace.entity.InviteCode;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import com.edumax.molip.domain.workspace.entity.WorkspaceRegister;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceMemberException;
import com.edumax.molip.domain.workspace.repository.InviteCodeRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceMemberRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceRegisterRepository;
import com.edumax.molip.domain.workspace.repository.WorkspaceRepository;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import com.edumax.molip.domain.workspace.types.WorkspaceRegisterStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
  private final InviteCodeRepository inviteCodeRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final WorkspaceRegisterRepository workspaceRegisterRepository;
  private final WorkspaceRepository workspaceRepository;

  private final MemberRepository memberRepository;

  @Override
  public List<FindWorkspaceResponse> findWorkspaces(Long memberId) {
    List<Workspace> workspaceList = workspaceRepository.findAll();
    List<FindWorkspaceResponse> result = new ArrayList<>();
    workspaceList.forEach(workspace -> result.add(findWorkspace(workspace, memberId)));
    return result;
  }

  @Override
  public FindWorkspaceResponse findWorkspaceById(Long workspaceId, Long memberId) {
    Workspace workspace = workspaceRepository.findById(workspaceId)
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
    Boolean isSearchable = workspace.getIsSearchable();
    List<WorkspaceRegister> workspaceRegisters =
        workspace.getWorkspaceRegisters();
    List<InviteCode> inviteCodes =
        workspace.getInviteCodes();
    List<WorkspaceMember> workspaceMembers =
        workspace.getWorkspaceMembers();
    List<Course> courses = workspace.getCourses();

    WorkspaceRegister workspaceRegister =
        workspaceRegisterRepository.
            findByMemberAndWorkspace(member, workspace).orElse(null);
    String myStatus = "미신청";
    WorkspaceMemberRole myRole = null;
    /**
     * - workspaceRegister가 없으면 아얘 신청한 이력이 없는 것으로 간주합니다!!
     * - 워크스페이스에 들어갔다고 workspaceRegister 테이블에서 삭제되지 않음
     * - PENDING, REJECTED면 null (WorkspaceMember가 아니기 때문임)
     */
    if (workspaceRegister != null) {
      WorkspaceRegisterStatus status = workspaceRegister.getStatus();
      myStatus = status.getLabel(); // 가입 대기, 가입 완료, 반려됨
      if (status == WorkspaceRegisterStatus.APPROVE) {
        WorkspaceMember workspaceMember =
            workspaceMemberRepository.findByMemberAndWorkspace(member, workspace)
                .orElseThrow(NoWorkspaceMemberException::new);
        myRole = workspaceMember.getRole();
      }
    }

    return FindWorkspaceResponse.builder()
        .workspace_id(workspaceId)
        .name(name)
        .logo_url(logoUrl)
        .description(description)
        .memberCount(workspaceMembers.size())
        .courseCount(courses.size())
        .my_status(myStatus)
        .my_role(myRole)
        .build();
  }

  @Override
  public JoinWorkspaceResponse joinWorkspace(JoinWorkspaceRequest joinWorkspaceRequest) {
    Long workspaceId = joinWorkspaceRequest.getWorkspaceId();
    Workspace workspace = workspaceRepository.findById(workspaceId)
        .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler
    WorkspaceMemberRole role = joinWorkspaceRequest.getRole();
    Long member_id = joinWorkspaceRequest.getMember_id();
    Member member = memberRepository.findById(member_id)
        .orElseThrow(NoMemberException::new); // GlobalExceptionHandler
    WorkspaceRegisterStatus status = joinWorkspaceRequest.getStatus();
    LocalDateTime applied_at = joinWorkspaceRequest.getApplied_at();
    if (status == WorkspaceRegisterStatus.APPROVE) {

    } else if (status == WorkspaceRegisterStatus.REJECTED) {

    } else {
      // PENDING
      WorkspaceRegister workspaceRegister =
          WorkspaceRegister.builder()
          .build();
    }

    workspaceMemberRepository.save(WorkspaceMember.builder()
        .connectedAt(applied_at)
        .status(null) // status integer값?
        .role(null)
        .member(member)
        .workspace(workspace)
        .build());
    return JoinWorkspaceResponse.builder().apiResult(true).build();
  }

  @Override
  public FindMyStatsResponse findMyStats(Long workspaceId) { // 모르것다..?
    Workspace workspace = workspaceRepository.findById(workspaceId)
        .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler

    return null;
  }

  @Override
  public List<FindWorkspaceCoursesByIdResponse> findWorkspaceCoursesById(Long workspaceId) {
    Workspace workspace = workspaceRepository.findById(workspaceId)
        .orElseThrow(NoWorkspaceException::new); // GlobalExceptionHandler
    List<Course> courses = workspace.getCourses();
    List<FindWorkspaceCoursesByIdResponse> result = new ArrayList<>();
    courses.forEach(course -> {
      Long courseId = course.getCourseId();
      String title = course.getTitle();
      String description = course.getDescription();
      String subCategory = course.getSubCategory();
      Integer maxLearner = course.getMaxLearner();
      Boolean isEngageCheck = course.getIsEngageCheck();
      Boolean isSentimentAnalysis = course.getIsSentimentAnalysis();
      Integer attendance = course.getAttendance();
      Integer engagement = course.getEngagement();
      Integer exitQuiz = course.getExitQuiz();
      Course.CourseStatus status = course.getStatus();
      result.add(FindWorkspaceCoursesByIdResponse.builder()
              .course_id(courseId)
              .workspace_id(workspaceId)
              .title(title)
              .description(description)
              .category(null)
              .sub_category(subCategory)
              .tag(null)
              .progress_rate(null)
              .current_lessons(null)
              .is_mandatory(null)
              .is_liked(null)
              .d_day(null)
              .status(status.toString())
              .enrollment_status(null)
          .build());
    });
    return result;
  }
}
