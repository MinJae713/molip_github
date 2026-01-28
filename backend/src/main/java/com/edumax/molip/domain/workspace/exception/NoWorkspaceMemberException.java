package com.edumax.molip.domain.workspace.exception;

public class NoWorkspaceMemberException extends RuntimeException{
  public NoWorkspaceMemberException() {
    super("해당 워크스페이스 멤버가 없습니다");
  }
}
