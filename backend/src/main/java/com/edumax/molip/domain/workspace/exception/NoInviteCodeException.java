package com.edumax.molip.domain.workspace.exception;

public class NoInviteCodeException extends RuntimeException {
  public NoInviteCodeException() {
    super("해당 초대 코드가 없습니다");
  }
}
