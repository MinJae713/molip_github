package com.edumax.molip.domain.course.exception;

public class IncorrectCourseTrainerException extends RuntimeException {
  public IncorrectCourseTrainerException() {
    super("운영 강사 정보가 다릅니다");
  }
}
