package com.edumax.molip.domain.workspace.dto.response;

import com.edumax.molip.domain.workspace.types.WorkspaceMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindWorkspaceMemberByIdResponse {
  // 잠깐 대기 - 워크스페이스 멤버 상세조회임
  private String profileImageUrl;
  private String name;
  private WorkspaceMemberRole role;
}
