package com.edumax.molip.domain.course.exception;

public class NoZzimException extends RuntimeException {
  public NoZzimException() {
    super("해당 강의 찜 기록이 없습니다");
  }
}
