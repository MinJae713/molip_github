package com.edumax.molip.domain.course.exception;

public class IncorrenctWorkspaceCourseException extends RuntimeException {
  public IncorrenctWorkspaceCourseException() {
    super("강의 정보와 워크스페이스 정보가 다릅니다");
  }
}
