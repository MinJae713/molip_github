package com.edumax.molip.common.exception;

import com.edumax.molip.domain.course.exception.IncorrenctWorkspaceCourseException;
import com.edumax.molip.domain.course.exception.NoCourseException;
import com.edumax.molip.domain.course.exception.NoZzimException;
import com.edumax.molip.domain.member.exception.DuplicateEmailException;
import com.edumax.molip.domain.member.exception.NoAdminException;
import com.edumax.molip.domain.member.exception.NoMemberException;
import com.edumax.molip.domain.workspace.exception.InviteCodeExistException;
import com.edumax.molip.domain.workspace.exception.NoInviteCodeException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceAppliedAtException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceMemberException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceMemberRoleException;
import com.edumax.molip.domain.workspace.exception.NoWorkspaceRegisterException;
import com.edumax.molip.domain.workspace.exception.WorkspaceMemberExistException;
import com.edumax.molip.domain.workspace.exception.WorkspaceRegisterExistException;
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
  @ExceptionHandler({NoMemberException.class, NoAdminException.class})
  public ResponseEntity<ErrorResponse> handleMemberBadRequestException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler({
    NoWorkspaceException.class,
    NoWorkspaceMemberException.class,
    NoWorkspaceRegisterException.class,
    WorkspaceRegisterExistException.class,
    WorkspaceMemberExistException.class,
    NoWorkspaceMemberRoleException.class,
    NoWorkspaceAppliedAtException.class,
    InviteCodeExistException.class,
    NoInviteCodeException.class
  })
  public ResponseEntity<ErrorResponse> handleWorkspaceBadRequestException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler({
    NoCourseException.class,
    NoZzimException.class,
    IncorrenctWorkspaceCourseException.class
  })
  public ResponseEntity<ErrorResponse> handleCourseBadRequestException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}
