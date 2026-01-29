package com.edumax.molip.domain.workspace.types;

import lombok.Getter;

@Getter
public enum WorkspaceRegisterStatus {
  PENDING("가입 대기"),
  APPROVE("가입 완료"),
  REJECTED("반려됨");
  private final String label;

  WorkspaceRegisterStatus(String label) {
    this.label = label;
  }
}
