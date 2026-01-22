package com.edumax.molip.domain.auth.repository;

import com.edumax.molip.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<Member> findByEmailAndDeletedAtIsNull(String email);

  Optional<Member> findByIdAndDeletedAtIsNull(Long memberId);
}
