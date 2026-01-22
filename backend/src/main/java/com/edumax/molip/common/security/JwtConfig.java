package com.edumax.molip.common.security;

import com.edumax.molip.common.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class JwtConfig {
  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.access-token.expiration-minute}")
  private long accessTokenValidMinute;

  @Value("${jwt.refresh-token.expiration-day}")
  private long refreshTokenValidDay;

  @Bean
  public JwtTokenProvider jwtTokenProvider(UserDetailsService userDetailsService) {
    long accessTime = accessTokenValidMinute * 60 * 1000;
    long refreshTime = refreshTokenValidDay * 24 * 60 * 60 * 1000;
    return new JwtTokenProvider(secretKey, accessTime, refreshTime, userDetailsService);
  }
}
