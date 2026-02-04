package com.edumax.molip.domain.course.exception;

public class IncorrectWorkspaceCourseException extends RuntimeException {
  public IncorrectWorkspaceCourseException() {
    super("강의 정보와 워크스페이스 정보가 다릅니다");
  }
}
