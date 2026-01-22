package com.edumax.molip.domain.member.service;

import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.member.exception.DuplicateEmailException;
import com.edumax.molip.domain.member.repository.MemberRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
  private final StringRedisTemplate redisTemplate;
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Value("${jwt.refresh-token.expiration-day}")
  private Long refreshTokenExpirationDay;

  private String REFRESH_TOKEN_KEY_FORMAT = "jwt:refresh:%d";

  public void updateRefreshToken(Long memberId, String refreshToken) {
    Duration ttl = Duration.ofDays(refreshTokenExpirationDay);
    String redisKey = REFRESH_TOKEN_KEY_FORMAT.formatted(memberId);
    if (refreshToken == null) {
      ttl = Duration.ofSeconds(0); // 삭제 처리
      refreshToken = "";
    }
    redisTemplate.opsForValue().set(redisKey, refreshToken, ttl);
  }

  public String getRefreshToken(Long memberId) {
    String redisKey = REFRESH_TOKEN_KEY_FORMAT.formatted(memberId);
    return redisTemplate.opsForValue().get(redisKey);
  }

  @Transactional
  public void registerMember(Member member, Member.AuthProvider authProvider) {
    Member memberResult =
        memberRepository.findByEmailAndDeletedAtIsNull(member.getEmail()).orElse(null);

    if (memberResult != null) { // 이미 존재하는 이메일
      throw new DuplicateEmailException("이미 존재하는 이메일입니다. email: " + member.getEmail());
    }

    member.setPasswordHash(passwordEncoder.encode(member.getPasswordHash()));

    try {
      memberRepository.saveAndFlush(member);
    } catch (DataIntegrityViolationException e) { // 동시성 문제로 인해 중복 이메일이 발생
      throw new DuplicateEmailException("회원가입에 실패했습니다. email: " + member.getEmail(), e);
    }
  }

  public Member findById(Long memberId) {
    return memberRepository
        .findByIdAndDeletedAtIsNull(memberId)
        .orElseThrow(() -> new RuntimeException("해당 아이디의 사용자를 찾을 수 없습니다. userId: " + memberId));
  }

  public Member findByUserEmail(String userEmail) {
    return memberRepository
        .findByEmailAndDeletedAtIsNull(userEmail)
        .orElseThrow(() -> new RuntimeException("해당 이메일의 사용자를 찾을 수 없습니다. email: " + userEmail));
  }

  @Transactional
  public void deleteMember(Long userId) {
    Member memberResult = memberRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);

    if (memberResult == null) {
      throw new RuntimeException("회원 삭제를 실패했습니다. userId: " + userId);
    }

    memberResult.toggleDelete();
  }

  @Transactional
  public void updateMember(Member member) {
    Member memberResult = memberRepository.findByIdAndDeletedAtIsNull(member.getId()).orElse(null);
    if (memberResult == null) {
      throw new RuntimeException("존재하지 않는 회원입니다. userId: " + member.getId());
    }

    memberResult.update(
        member.getPasswordHash() == null
            ? memberResult.getPasswordHash()
            : passwordEncoder.encode(member.getPasswordHash()),
        member.getName(),
        member.getProfileImageUrl(),
        member.getAge(),
        member.getAddress(),
        member.getPhoneNumber(),
        member.getPosition());
  }
}
