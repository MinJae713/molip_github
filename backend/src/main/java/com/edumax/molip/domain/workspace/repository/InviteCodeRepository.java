package com.edumax.molip.domain.workspace.repository;

import com.edumax.molip.domain.workspace.entity.InviteCode;
import com.edumax.molip.domain.workspace.entity.Workspace;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
  Optional<InviteCode> findByCodeName(String codeName);

  Optional<InviteCode> findByCodeNameAndWorkspace(String codeName, Workspace workspace);

  Optional<InviteCode> findByIdentifyValue(String identifyValue);
}
