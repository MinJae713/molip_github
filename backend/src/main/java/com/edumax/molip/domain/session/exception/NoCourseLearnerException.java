package com.edumax.molip.domain.session.exception;

public class NoCourseLearnerException extends RuntimeException {
  public NoCourseLearnerException() {
    super("해당 강의 수강생 정보가 없습니다");
  }
}
