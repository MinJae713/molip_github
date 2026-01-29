package com.edumax.molip.domain.workspace.repository;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.workspace.entity.Workspace;
import com.edumax.molip.domain.workspace.entity.WorkspaceMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
  Optional<WorkspaceMember> findByMemberAndWorkspace(Member member, Workspace workspace);

  List<WorkspaceMember> findByMember(Member member);
}
