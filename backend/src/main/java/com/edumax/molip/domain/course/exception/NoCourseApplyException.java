package com.edumax.molip.domain.course.exception;

public class NoCourseApplyException extends RuntimeException {
  public NoCourseApplyException() {
    super("해당 수강신청 내역이 없습니다");
  }
}
