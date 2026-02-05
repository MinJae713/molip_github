package com.edumax.molip.domain.workspace.repository;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.workspace.entity.Workspace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
  List<Workspace> findByNameContaining(String searchQuery);

  List<Workspace> findByMember(Member member);
}
