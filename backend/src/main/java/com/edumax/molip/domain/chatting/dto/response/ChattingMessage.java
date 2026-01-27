package com.edumax.molip.domain.chatting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChattingMessage {
  private Long memberId;
  private String email;
  private String name;
  private String profileImageUrl;
  private String content;
}
