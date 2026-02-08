package com.edumax.molip.domain.workspace.exception;

public class NoWorkspaceMemberRoleException extends RuntimeException {
  public NoWorkspaceMemberRoleException() {
    super("LEARNER 혹은 TRAINER로 지정해주세요");
  }
}
