package com.edumax.molip.auth.exception;

public class DuplicateEmailException extends RuntimeException {
  public DuplicateEmailException(String message) {
    super(message);
  }

  public DuplicateEmailException(String message, Throwable cause) {
    super(message, cause);
  }
}
