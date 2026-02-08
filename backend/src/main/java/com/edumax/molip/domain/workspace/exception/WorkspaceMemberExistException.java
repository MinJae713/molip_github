package com.edumax.molip.domain.workspace.exception;

public class WorkspaceMemberExistException extends RuntimeException {
  public WorkspaceMemberExistException() {
    super("이미 가입이 되어있는 멤버입니다");
  }
}
