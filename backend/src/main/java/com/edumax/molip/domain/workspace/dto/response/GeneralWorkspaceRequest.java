package com.edumax.molip.domain.workspace.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class GeneralWorkspaceRequest {
  @NotNull(message = "워크스페이스 이름은 필수값입니다.")
  @Size(max = 100, message = "워크스페이스 이름은 100자 이내여야 합니다.")
  protected String name;

  @Size(max = 300, message = "워크스페이스 설명은 300자 이내여야 합니다.")
  protected String description;

  protected String logoUrl; // 임시 - 실제 구현 시 MultipartFile로 변경 예정
}
