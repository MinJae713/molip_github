package com.edumax.molip.domain.chatting.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class QnaMessageSaveEvent {
  private LocalDateTime timestamp;
  private String question;
  private String answer;
  private Long sessionId;
  private Long senderId;
}
