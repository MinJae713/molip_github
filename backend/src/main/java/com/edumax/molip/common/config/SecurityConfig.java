package com.edumax.molip.common.config;

import com.edumax.molip.common.filter.LogFilter;
import com.edumax.molip.common.jwt.JwtAuthenticationFilter;
import com.edumax.molip.common.jwt.JwtTokenProvider;
import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.member.entity.Member;
import com.edumax.molip.domain.member.service.MemberService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(
        List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost",
            "https://70.12.246.41"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));

    // ✅ 브라우저에서 읽을 수 있게 노출할 헤더 (필요 시)
    config.setExposedHeaders(List.of("Authorization"));

    // ✅ 쿠키(세션, refreshToken 등)를 주고받을 거면 반드시 true
    config.setAllowCredentials(true);

    // ✅ preflight 캐시 시간(초) - 선택
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/assets/**",
                        "/*.ico",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error")
                    .permitAll()
                    .requestMatchers(
                        "/api/member/refresh",
                        "/api/member/login",
                        "/api/member/register",
                        "/api/member/logout")
                    .permitAll()
                    .requestMatchers("/uploads/**", "/message/**", "/emotion/**", "/voice/**")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterAt(
            new JwtAuthenticationFilter(jwtTokenProvider),
            UsernamePasswordAuthenticationFilter.class) // 교체
        .addFilterAfter(new LogFilter(), JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(MemberService memberService) {
    return new UserDetailsService() {
      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberService.findByUserEmail(username);
        try {
          member = memberService.findByUserEmail(username);
        } catch (RuntimeException e) {
          log.error("사용자 조회 실패 - email: {}", username);
          throw e;
        }
        log.debug("사용자 조회 성공 - email: {}", username);
        return new MemberUserDetails(member);
      }
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /*
   * 기본 auth Manager 빈 등록 방법
   *
   * UserDetailsService → SecurityConfig에 있는 Bean 자동 반영
   * PasswordEncoder → 자동 반영
   * DaoAuthenticationProvider → 자동 구성
   * AuthenticationManager → 자동 생성
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }
}
