package com.edumax.molip.domain.workspace.dto.request;

import com.edumax.molip.domain.workspace.dto.response.GeneralWorkspaceRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkspaceRequest extends GeneralWorkspaceRequest {
  @NotNull(message = "워크스페이스 아이디는 필수값입니다.")
  @Positive(message = "워크스페이스 아이디는 양수여야 합니다.")
  private Long workspace_id;
}
