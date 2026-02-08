package com.edumax.molip.domain.quiz.exception;

public class NoCreateExitQuizPermitException extends RuntimeException {
  public NoCreateExitQuizPermitException() {
    super("퀴즈 생성 권한이 없습니다");
  }
}
