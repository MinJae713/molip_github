package com.edumax.molip.domain.workspace.exception;

public class IncorrectWorkspaceMemberException extends RuntimeException {
  public IncorrectWorkspaceMemberException() {
    super("해당 워크스페이스에 속한 멤버가 아닙니다");
  }
}
