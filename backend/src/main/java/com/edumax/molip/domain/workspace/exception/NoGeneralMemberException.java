package com.edumax.molip.domain.workspace.exception;

public class NoGeneralMemberException extends RuntimeException {
  public NoGeneralMemberException() {
    super("일반 유저가 아닌 사용자입니다");
  }
}
