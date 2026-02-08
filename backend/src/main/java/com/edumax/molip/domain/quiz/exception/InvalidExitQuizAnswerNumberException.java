package com.edumax.molip.domain.quiz.exception;

public class InvalidExitQuizAnswerNumberException extends RuntimeException {
  public InvalidExitQuizAnswerNumberException() {
    super("정답 번호는 답안의 개수보다 클 수 없습니다");
  }
}
