package com.edumax.molip.domain.quiz.controller;

import com.edumax.molip.domain.quiz.dto.QuestionMessage;
import com.edumax.molip.domain.quiz.service.GrpcClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {
  private final GrpcClientService grpcClientService;
  private final SimpMessagingTemplate messagingTemplate;

  @GetMapping("/test/ping")
  public String test(@RequestParam String msg) {
    return grpcClientService.sendPing(msg);
  }

  @MessageMapping("/quiz/{roomId}")
  public void makeQuiz(@DestinationVariable String roomId, QuestionMessage message) {

    messagingTemplate.convertAndSend("/topic/room/" + roomId, "message");
  }
}
