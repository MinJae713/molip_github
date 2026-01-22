package com.edumax.molip.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class LogFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long start = System.currentTimeMillis();
    String uri = request.getRequestURI();
    String method = request.getMethod();

    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - start;
      int status = response.getStatus();

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "|anonymous|";

      log.info("{} : [{}] {} - {} ({} ms)", username, method, uri, status, duration);
    }
  }
}
