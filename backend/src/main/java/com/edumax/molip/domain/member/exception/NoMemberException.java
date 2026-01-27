package com.edumax.molip.domain.member.exception;

public class NoMemberException extends RuntimeException{
  public NoMemberException() {
    super("해당 사용자가 없습니다");
  }
}
