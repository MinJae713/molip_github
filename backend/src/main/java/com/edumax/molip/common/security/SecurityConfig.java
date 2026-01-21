package com.edumax.molip.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()) // (API 서버면 일단 disable로 시작 후, 필요시 보강)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable());

    return http.build();
  }
}
