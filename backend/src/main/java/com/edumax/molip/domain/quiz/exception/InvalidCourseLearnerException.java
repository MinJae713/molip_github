package com.edumax.molip.domain.quiz.exception;

public class InvalidCourseLearnerException extends RuntimeException {
  public InvalidCourseLearnerException() {
    super("해당 강의의 수강생이 아닙니다");
  }
}
