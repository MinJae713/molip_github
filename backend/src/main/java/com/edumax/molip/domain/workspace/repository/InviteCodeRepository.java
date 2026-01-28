package com.edumax.molip.domain.workspace.repository;

import com.edumax.molip.domain.workspace.entity.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
  Optional<InviteCode> findByCodeName(String codeName);
}
