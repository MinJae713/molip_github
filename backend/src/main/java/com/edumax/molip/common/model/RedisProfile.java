package com.edumax.molip.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisProfile {
  private String email;
  private Long memberId;
  private String name;
  private String profileImageUrl;
}
