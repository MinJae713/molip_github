package com.edumax.molip.domain.workspace.exception;

public class ExpiredInviteCodeException extends RuntimeException {
  public ExpiredInviteCodeException() {
    super("만료된 초대코드입니다");
  }
}
