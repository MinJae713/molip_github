package com.edumax.molip.domain.workspace.exception;

public class WorkspaceRegisterExistException extends RuntimeException {
  public WorkspaceRegisterExistException() {
    super("이미 가입 신청이 되어있는 멤버입니다");
  }
}
