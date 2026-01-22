package com.edumax.molip.domain.member.repository;

import com.edumax.molip.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<Member> findByEmailAndDeletedAtIsNull(String email);

  Optional<Member> findByIdAndDeletedAtIsNull(Long memberId);
}
