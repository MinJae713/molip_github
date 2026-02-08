package com.edumax.molip.domain.session.exception;

public class NoSessionException extends RuntimeException {
  public NoSessionException() {
    super("해당 세션이 없습니다");
  }
}
