package com.edumax.molip.domain.member.repository;

import com.edumax.molip.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {}
