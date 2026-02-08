package com.edumax.molip.domain.quiz.exception;

public class NoExitQuizResultException extends RuntimeException {
  public NoExitQuizResultException() {
    super("해당 퇴실 퀴즈 결과가 없습니다");
  }
}
