package com.edumax.molip.domain.workspace.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJoinCodeRequest {

  @NotNull(message = "워크 스페이스 아이디는 필수값입니다.")
  @Positive(message = "워크 스페이스 아이디는 양수여야 합니다.")
  private Long workspaceId;

  @NotNull(message = "만료 일자는 필수 값입니다.")
  private LocalDateTime endedDate;

  @NotNull(message = "코드 이름은 필수 값입니다.")
  private String joinCodeName;
}
