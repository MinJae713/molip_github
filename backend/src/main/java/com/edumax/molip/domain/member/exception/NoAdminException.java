package com.edumax.molip.domain.member.exception;

public class NoAdminException extends RuntimeException{
  public NoAdminException() {
    super("관리자가 아닌 사용자입니다");
  }
}
