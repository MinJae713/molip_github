package com.edumax.molip.domain.workspace.exception;

public class NoWorkspaceException extends RuntimeException{
  public NoWorkspaceException() {
    super("해당 워크스페이스가 없습니다");
  }
}
