package com.edumax.molip.domain.workspace.repository;

import com.edumax.molip.domain.workspace.entity.InviteCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
  Optional<InviteCode> findByCodeName(String codeName);
}
