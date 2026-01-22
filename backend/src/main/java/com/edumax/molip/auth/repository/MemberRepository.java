package com.edumax.molip.auth.repository;

import com.edumax.molip.auth.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<Member> findByEmailAndDeletedAtIsNull(String email);

  Optional<Member> findByIdAndDeletedAtIsNull(Long memberId);
}
