package com.edumax.molip.common.exception;

import com.edumax.molip.domain.member.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.CONFLICT, e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  //  @ExceptionHandler(Exception.class)
  //  public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
  //    ErrorResponse body = ErrorResponse.create(e, HttpStatus.INTERNAL_SERVER_ERROR, "예상하지 못한 서버에러
  // 발생");
  //    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  //  }
}
