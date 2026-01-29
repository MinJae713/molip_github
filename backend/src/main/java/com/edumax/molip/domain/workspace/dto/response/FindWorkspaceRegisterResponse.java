package com.edumax.molip.domain.workspace.dto.response;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.workspace.entity.WorkspaceRegister;
import com.edumax.molip.domain.workspace.types.RejectType;
import com.edumax.molip.domain.workspace.types.WorkspaceRegisterStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindWorkspaceRegisterResponse {
  private String profileImageUrl;
  private String name;
  private String email;
  private LocalDateTime createdAt; // 신청일
  private WorkspaceRegisterStatus status;

  private LocalDateTime approvedAt;
  private String rejectReason;
  private RejectType rejectType;

  public static FindWorkspaceRegisterResponse from(WorkspaceRegister workspaceRegister) {
    Member member = workspaceRegister.getMember();
    WorkspaceRegisterStatus status = workspaceRegister.getStatus();

    FindWorkspaceRegisterResponseBuilder builder =
        FindWorkspaceRegisterResponse.builder()
            .profileImageUrl(member.getProfileImageUrl())
            .name(member.getName())
            .email(member.getEmail())
            .createdAt(workspaceRegister.getCreatedAt())
            .status(status);

    if (status != WorkspaceRegisterStatus.PENDING) {
      builder.approvedAt(workspaceRegister.getApprovedAt()); // 신청일 반려일 모두 해당!
      if (status == WorkspaceRegisterStatus.REJECTED) {
        builder
            .rejectReason(workspaceRegister.getRejectReason())
            .rejectType(workspaceRegister.getRejectType());
      }
    }
    return builder.build();
  }
}
