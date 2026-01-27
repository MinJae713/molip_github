package com.edumax.molip.domain.workspace.repository;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRegisterRepository extends JpaRepository<WorkspaceRegister, Long> {
  Optional<WorkspaceRegister> findByMemberAndWorkspace(Member member, Workspace workspace);
}
