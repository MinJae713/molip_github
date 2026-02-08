package com.edumax.molip.domain.course.exception;

public class AlreadyAppliedCourseApplyException extends RuntimeException {
  public AlreadyAppliedCourseApplyException() {
    super("이미 수락이 완료된 수강신청 내역입니다");
  }
}
