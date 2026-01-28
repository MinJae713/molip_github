package com.edumax.molip.domain.workspace.exception;

public class NoWorkspaceAppliedAtException extends RuntimeException{
  public NoWorkspaceAppliedAtException() {
    super("가입 승인 날짜를 지정해주세요");
  }
}
