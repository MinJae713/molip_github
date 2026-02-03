package com.edumax.molip.domain.quiz.controller;

import com.edumax.molip.domain.quiz.dto.response.AiQuizMessage;
import com.edumax.molip.domain.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {
  private final SimpMessagingTemplate messagingTemplate;
  private final QuizService quizService;

  @MessageMapping("/quiz/{roomId}")
  public void makeQuiz(@DestinationVariable String roomId) {
    AiQuizMessage aiQuizMessage = quizService.createQuizRedis(roomId, 100);
    messagingTemplate.convertAndSend("/topic/quiz/" + roomId, aiQuizMessage);
  }

  @MessageMapping("/quiz/send/{roomId}")
  public void sendQuiz(@DestinationVariable String roomId, AiQuizMessage quizMessage) {
    messagingTemplate.convertAndSend("/topic/surprise-quiz/" + roomId, quizMessage);
  }
}
