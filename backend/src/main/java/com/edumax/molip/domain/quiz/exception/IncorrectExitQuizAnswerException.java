package com.edumax.molip.domain.quiz.exception;

public class IncorrectExitQuizAnswerException extends RuntimeException {
  public IncorrectExitQuizAnswerException() {
    super("푼 문제 갯수가 다릅니다");
  }
}
