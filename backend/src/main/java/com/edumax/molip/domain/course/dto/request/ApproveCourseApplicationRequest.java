package com.edumax.molip.domain.course.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveCourseApplicationRequest {

  @NotNull(message = "워크스페이스 멤버 아이디는 필수값입니다.")
  @Positive(message = "워크스페이스 멤버 아이디는 양수여야 합니다.")
  private Long workspaceMemberId; // 승인 멤버 Id

  @NotNull(message = "승인 여부는 필수여야 합니다.")
  private Boolean isApply; // 승인 여부
}
