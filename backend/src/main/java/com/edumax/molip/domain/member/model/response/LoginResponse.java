package com.edumax.molip.domain.member.model.response;

import com.edumax.molip.domain.member.entity.Member;
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
  private Member.Role role;
}
