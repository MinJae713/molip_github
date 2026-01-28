package com.edumax.molip.domain.chatting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionMessage {
  private Long memberId;
  private String questionId;
  private String content;
  private String name;
  private String timestamp;
}
