package com.edumax.molip.domain.workspace.exception;

public class InviteCodeExistException extends RuntimeException {
  public InviteCodeExistException() {
    super("이미 존재하는 초대 코드입니다");
  }
}
