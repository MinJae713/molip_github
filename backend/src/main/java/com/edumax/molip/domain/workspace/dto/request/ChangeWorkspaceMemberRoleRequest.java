package com.edumax.molip.domain.workspace.dto.request;

import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeWorkspaceMemberRoleRequest {

  @NotNull(message = "워크스페이스 멤버 아이디는 필수값입니다.")
  @Positive(message = "워크스페이스 멤버 아이디는 양수여야 합니다.")
  private Long workspace_member_id;

  @NotNull(message = "멤버 역할은 필수값입니다.")
  private WorkspaceMemberRole workspaceMemberRole;
}
