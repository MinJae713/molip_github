package com.edumax.molip.domain.workspace.exception;

public class NoTrainerException extends RuntimeException {
  public NoTrainerException() {
    super("강사가 아닌 사용자입니다");
  }
}
