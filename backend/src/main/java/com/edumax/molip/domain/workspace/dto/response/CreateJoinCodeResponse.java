package com.edumax.molip.domain.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJoinCodeResponse {
  private String identifyValue; // 생성된 식별 값
}
