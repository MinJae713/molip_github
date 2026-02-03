package com.edumax.molip.domain.chatting.controller;

import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.chatting.dto.response.ChattingMessage;
import com.edumax.molip.domain.chatting.dto.response.QuestionMessage;
import com.edumax.molip.domain.chatting.service.ChattingService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatting")
@RequiredArgsConstructor
public class ChattingController {
  private final SimpMessagingTemplate messagingTemplate;
  private final ChattingService chattingService;

  @MessageMapping("/chat-room/{roomId}")
  public void sendChatting(
      SendMessage sendMessage, @DestinationVariable String roomId, Authentication authentication) {
    MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();

    if ("chat".equals(sendMessage.type())) {
      ChattingMessage message =
          ChattingMessage.builder()
              .memberId(userDetails.getMemberId())
              .email(userDetails.getUsername())
              .name(userDetails.getDisplayName())
              .profileImageUrl(userDetails.getProfileImageUrl())
              .content(sendMessage.content)
              .timestamp(LocalDateTime.now().toString())
              .build();
      messagingTemplate.convertAndSend("/topic/chat-room/" + roomId, message);
    } else if ("question".equals(sendMessage.type())) {
      QuestionMessage message =
          QuestionMessage.builder()
              .memberId(userDetails.getMemberId())
              .questionId(sendMessage.questionId())
              .content(sendMessage.content)
              .name(userDetails.getDisplayName())
              .profileImageUrl(userDetails.getProfileImageUrl())
              .timestamp(LocalDateTime.now().toString())
              .build();
      messagingTemplate.convertAndSend("/topic/chat-room/" + roomId, message);
    } else {
      throw new IllegalArgumentException("Invalid message type: " + sendMessage.type());
    }
  }

  public record SendMessage(
      String type, // chat, question
      String content,
      String questionId // QUESTION 타입일 때만 사용
      ) {}

  @GetMapping("/qna-room/{sessionId}/{memberId}")
  public ResponseEntity<List<QnaMessageResponse>> getQnaMessages(
      @PathVariable Long sessionId, @PathVariable Long memberId) {

    List<QnaMessageResponse> qnaMessageResponseList =
        chattingService.readAllMessages(sessionId, memberId).stream()
            .map(
                message ->
                    new QnaMessageResponse(
                        message.getCreatedAt(), message.getSenderId(), message.getMessage()))
            .toList();

    return ResponseEntity.ok(qnaMessageResponseList);
  }

  public record QnaMessageResponse(LocalDateTime timestamp, Long senderId, String content) {}

  @MessageMapping("/qna-room/{sessionId}/{memberId}")
  public void sendQnAMessage(
      QnAMessage qnaMessage,
      @DestinationVariable Long sessionId,
      @DestinationVariable Long memberId) {
    chattingService.createChatRoom(sessionId, memberId);
    QnaMessageResponse aiMessageResponse =
        chattingService.getAiAnswerAndSaveMessage(sessionId, memberId, qnaMessage.content);

    messagingTemplate.convertAndSend(
        "/topic/qna-room/" + sessionId + "/" + memberId, aiMessageResponse);
  }

  public class QnAMessage {
    private String content;
  }
}
