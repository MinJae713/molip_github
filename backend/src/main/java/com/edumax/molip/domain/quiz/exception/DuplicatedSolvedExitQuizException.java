package com.edumax.molip.domain.quiz.exception;

public class DuplicatedSolvedExitQuizException extends RuntimeException {
  public DuplicatedSolvedExitQuizException() {
    super("이미 푼 퇴실 퀴즈 문제입니다");
  }
}
