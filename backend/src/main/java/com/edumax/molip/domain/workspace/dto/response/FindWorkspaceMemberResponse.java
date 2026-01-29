package com.edumax.molip.domain.workspace.dto.response;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import com.edumax.molip.domain.workspace.types.WorkspaceMemberStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindWorkspaceMemberResponse {
  private String profileImageUrl;
  private String name;
  private WorkspaceMemberRole role;
  private LocalDateTime createdAt; // 가입일
  private LocalDateTime connectedAt; // 최근 접속일
  private WorkspaceMemberStatus status; // 상태

  public static FindWorkspaceMemberResponse from(WorkspaceMember workspaceMember) {
    Member member = workspaceMember.getMember();
    return FindWorkspaceMemberResponse.builder()
        .profileImageUrl(member.getProfileImageUrl())
        .name(member.getName())
        .role(workspaceMember.getRole())
        .createdAt(workspaceMember.getCreatedAt())
        .connectedAt(workspaceMember.getConnectedAt())
        .status(workspaceMember.getStatus())
        .build();
  }
}
