package com.edumax.molip.domain.workspace.exception;

public class NoWorkspaceAppliedAtException extends RuntimeException {
  public NoWorkspaceAppliedAtException() {
    super("승인/반려 날짜를 지정해주세요");
  }
}
