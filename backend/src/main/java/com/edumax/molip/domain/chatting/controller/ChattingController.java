package com.edumax.molip.domain.chatting.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.chatting.dto.response.ChattingMessage;
import com.edumax.molip.domain.chatting.dto.response.QuestionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatting")
@RequiredArgsConstructor
public class ChattingController {
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/chat-room/{roomId}")
  public void sendChatting(
      SendChattingMessage sendMessage,
      @DestinationVariable String roomId,
      Authentication authentication) {
    MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();
    ChattingMessage message =
        ChattingMessage.builder()
            .memberId(userDetails.getMemberId())
            .email(userDetails.getUsername())
            .name(userDetails.getDisplayName())
            .profileImageUrl(userDetails.getProfileImageUrl())
            .content(sendMessage.content)
            .build();
    messagingTemplate.convertAndSend("/topic/chat-room/" + roomId, message);
  }

  public record SendChattingMessage(String content) {}

  @MessageMapping("/question-room/{roomId}")
  public void sendQuestionAnswer(
      SendQuestionMessage sendQuestionMessage,
      @DestinationVariable String roomId,
      Authentication authentication) {
    MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();
    QuestionMessage message =
        QuestionMessage.builder()
            .memberId(userDetails.getMemberId())
            .questionId(sendQuestionMessage.questionId())
            .content(sendQuestionMessage.content)
            .build();

    messagingTemplate.convertAndSend("/topic/question-room/" + roomId, message);
  }

  // 새로운 questionId를 보내면 질문이고, 기존에 있던 questionId를 보내면 답변이다.
  public record SendQuestionMessage(String questionId, String content) {}
}
