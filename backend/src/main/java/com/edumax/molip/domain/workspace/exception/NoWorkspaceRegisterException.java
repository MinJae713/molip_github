package com.edumax.molip.domain.workspace.exception;

public class NoWorkspaceRegisterException extends RuntimeException {
  public NoWorkspaceRegisterException() {
    super("해당 워크스페이스 가입 신청이 없습니다");
  }
}
