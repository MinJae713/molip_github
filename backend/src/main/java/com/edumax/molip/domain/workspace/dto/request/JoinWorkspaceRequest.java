package com.edumax.molip.domain.workspace.dto.request;

import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import com.edumax.molip.domain.workspace.types.WorkspaceRegisterStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinWorkspaceRequest {
  @NotNull(message = "워크스페이스 아이디는 필수값입니다.")
  @Positive(message = "워크스페이스 아이디는 양수여야 합니다.")
  private Long workspaceId;

  private WorkspaceMemberRole role;

  @NotNull(message = "멤버 아이디는 필수값입니다.")
  @Positive(message = "멤버 아이디는 양수여야 합니다.")
  private Long member_id;

  private WorkspaceRegisterStatus status;

  private LocalDateTime applied_at;
}
