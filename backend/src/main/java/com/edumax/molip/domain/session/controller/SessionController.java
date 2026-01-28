package com.edumax.molip.domain.session.controller;

import com.edumax.molip.domain.session.service.SessionServiceImpl;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/session")
public class SessionController {
  private final SessionServiceImpl sessionService;

  @PostMapping("/start/{sessionId}")
  public ResponseEntity<Map<String, String>> startCourse(@PathVariable String sessionId) {
    sessionService.startCourse(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "강의 시작"));
  }

  @PostMapping("/end/{sessionId}")
  public ResponseEntity<?> endCourse(@PathVariable String sessionId) {
    sessionService.endCourse(sessionId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "강의 종료"));
  }
}
