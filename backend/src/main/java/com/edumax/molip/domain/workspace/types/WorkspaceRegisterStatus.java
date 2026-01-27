package com.edumax.molip.domain.workspace.types;

public enum WorkspaceRegisterStatus {
  PENDING("가입 대기"),
  APPROVE("가입 완료"),
  REJECTED("반려됨");
  private String label;
  private WorkspaceRegisterStatus(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
