package com.edumax.molip.domain.course.exception;

public class NoCourseException extends RuntimeException {
  public NoCourseException() {
    super("해당 강의가 없습니다");
  }
}
