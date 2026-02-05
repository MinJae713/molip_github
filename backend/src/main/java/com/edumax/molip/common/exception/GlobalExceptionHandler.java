package com.edumax.molip.common.exception;

import com.edumax.molip.domain.course.exception.IncorrectCourseTrainerException;
import com.edumax.molip.domain.course.exception.IncorrectWorkspaceCourseException;
import com.edumax.molip.domain.course.exception.NoCourseException;
import com.edumax.molip.domain.course.exception.NoZzimException;
import com.edumax.molip.domain.member.exception.DuplicateEmailException;
import com.edumax.molip.domain.member.exception.NoAdminException;
import com.edumax.molip.domain.member.exception.NoMemberException;
import com.edumax.molip.domain.quiz.exception.DuplicatedSolvedExitQuizException;
import com.edumax.molip.domain.quiz.exception.IncorrectExitQuizAnswerException;
import com.edumax.molip.domain.quiz.exception.InvalidCourseLearnerException;
import com.edumax.molip.domain.quiz.exception.InvalidExitQuizAnswerNumberException;
import com.edumax.molip.domain.quiz.exception.NoCreateExitQuizPermitException;
import com.edumax.molip.domain.quiz.exception.NoExitQuizAnswerException;
import com.edumax.molip.domain.quiz.exception.NoExitQuizException;
import com.edumax.molip.domain.quiz.exception.NoExitQuizResultException;
import com.edumax.molip.domain.session.exception.NoCourseLearnerException;
import com.edumax.molip.domain.session.exception.NoSessionException;
import com.edumax.molip.domain.workspace.exception.ExpiredInviteCodeException;
import com.edumax.molip.domain.workspace.exception.InviteCodeExistException;
import com.edumax.molip.domain.workspace.exception.NoInviteCodeException;
import com.edumax.molip.domain.workspace.exception.NoTrainerException;
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
    NoInviteCodeException.class,
    NoTrainerException.class
  })
  public ResponseEntity<ErrorResponse> handleWorkspaceBadRequestException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler({ExpiredInviteCodeException.class})
  public ResponseEntity<ErrorResponse> handleWorkspaceNotAcceptableException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.NOT_ACCEPTABLE, e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(body);
  }

  @ExceptionHandler({
    NoCourseException.class,
    NoZzimException.class,
    IncorrectWorkspaceCourseException.class,
    IncorrectCourseTrainerException.class
  })
  public ResponseEntity<ErrorResponse> handleCourseBadRequestException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler({NoCourseLearnerException.class, NoSessionException.class})
  public ResponseEntity<ErrorResponse> handleSessionBadRequestException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler({
    NoCreateExitQuizPermitException.class,
    NoExitQuizException.class,
    NoExitQuizAnswerException.class,
    NoExitQuizResultException.class,
    InvalidExitQuizAnswerNumberException.class,
    InvalidCourseLearnerException.class,
    IncorrectExitQuizAnswerException.class,
    DuplicatedSolvedExitQuizException.class
  })
  public ResponseEntity<ErrorResponse> handleQuizBadRequestException(RuntimeException e) {
    ErrorResponse body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}
