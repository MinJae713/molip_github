package com.edumax.molip.domain.quiz.exception;

public class NoExitQuizAnswerException extends RuntimeException {
  public NoExitQuizAnswerException() {
    super("해당 퇴실 퀴즈 답안이 없습니다");
  }
}
