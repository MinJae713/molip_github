package com.edumax.molip.domain.workspace.repository;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
  Optional<WorkspaceMember> findByMemberAndWorkspace(Member member, Workspace workspace);
}
