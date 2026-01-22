package com.edumax.molip.common.jwt;

import com.edumax.molip.common.model.MemberUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
public class JwtTokenProvider {

  private final Key secretKey;
  private final long accessTokenValidMs;
  private final long refreshTokenValidMs;
  private final UserDetailsService userDetailsService;

  public JwtTokenProvider(
      String secret,
      long accessTokenValidMs,
      long refreshTokenValidMs,
      UserDetailsService userDetailsService) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.accessTokenValidMs = accessTokenValidMs;
    this.refreshTokenValidMs = refreshTokenValidMs;
    this.userDetailsService = userDetailsService;
  }

  // 액세스 토큰 생성
  public String generateAccessToken(MemberUserDetails user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenValidMs);

    return Jwts.builder()
        .claims()
        .subject(user.getUsername()) // username(email)
        .issuedAt(now)
        .expiration(expiry)
        .add("memberId", user.getMemberId())
        .add("name", user.getDisplayName())
        .add("type", "access")
        .and()
        .signWith(secretKey)
        .compact();
  }

  public String generateAccessToken(String email, Long memberId, String name) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenValidMs);

    return Jwts.builder()
        .claims()
        .subject(email) // username(email)
        .issuedAt(now)
        .expiration(expiry)
        .add("memberId", memberId)
        .add("name", name)
        .add("type", "access")
        .and()
        .signWith(secretKey)
        .compact();
  }

  public String generateRefreshToken(MemberUserDetails user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshTokenValidMs);

    return Jwts.builder()
        .claims()
        .subject(user.getUsername()) // username(email)
        .issuedAt(now)
        .expiration(expiry)
        .add("type", "refresh")
        .add("memberId", user.getMemberId())
        .and()
        .signWith(secretKey)
        .compact();
  }

  // 토큰에서 Authentication 생성
  public Authentication getAuthentication(String token) {
    String username = getUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  public String getUsername(String token) {
    Claims claims = parseClaims(token).getPayload();
    return claims.getSubject();
  }

  public Long getMemberId(String token) {
    Claims claims = parseClaims(token).getPayload();
    return claims.get("memberId", Long.class);
  }

  public boolean isRefreshToken(String token) {
    Claims claims = parseClaims(token).getPayload();
    String type = claims.get("type", String.class);
    return "refresh".equals(type);
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception e) {
      throw new RuntimeException("유효하지 않은 JWT 토큰입니다.", e);
    }
  }

  // secretKey 타입은 Key 또는 SecretKey
  private Jws<Claims> parseClaims(String token) {
    return Jwts.parser()
        .verifyWith((SecretKey) secretKey)
        .build()
        .parseSignedClaims(token); // 서명 검증 + Claims 검증 (만료)
  }

  public Duration getRefreshTokenValiditySeconds() {
    return Duration.ofMillis(refreshTokenValidMs);
  }
}
