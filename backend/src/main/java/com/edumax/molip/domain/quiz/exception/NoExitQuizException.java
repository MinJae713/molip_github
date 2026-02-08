package com.edumax.molip.domain.quiz.exception;

public class NoExitQuizException extends RuntimeException {
  public NoExitQuizException() {
    super("해당 퇴실 퀴즈가 없습니다");
  }
}
