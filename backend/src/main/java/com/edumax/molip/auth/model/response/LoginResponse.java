package com.edumax.molip.auth.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
  private String accessToken; // 발급된 JWT
  private String message;
  private String displayName; // 프론트에서 보여줄 사용자 이름
  private Long memberId;
  private String email;
  private String profileImageUrl;
}
