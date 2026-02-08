package com.edumax.molip.domain.workspace.exception;

public class AlreadyWorkspaceRegisterException extends RuntimeException {
  public AlreadyWorkspaceRegisterException() {
    super("이미 해당 워크스페이스에 등록된 사용자입니다");
  }
}
