package com.edumax.molip.domain.workspace.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InactiveWorkspaceMemberRequest {

  @NotNull(message = "워크스페이스 멤버 아이디는 필수값입니다.")
  @Positive(message = "워크스페이스 멤버 아이디는 양수여야 합니다.")
  private Long workspace_member_id;

  private Boolean is_send_mail;
  // 와이어 프레임 내용 반영 - 현재는 미구현 상태
}
