package com.edumax.molip.domain.workspace.dto.response;

import com.edumax.molip.domain.workspace.entity.InviteCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindJoinCodeResponse {
  private Long joinCodeId;
  private String joinCodeName;
  private String identifyValue;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime endedDate;

  public static FindJoinCodeResponse fromInviteCode(InviteCode inviteCode) {
    return FindJoinCodeResponse.builder()
        .joinCodeId(inviteCode.getInviteCodeId())
        .joinCodeName(inviteCode.getCodeName())
        .identifyValue(inviteCode.getIdentifyValue())
        .createdAt(inviteCode.getCreatedAt())
        .endedDate(inviteCode.getExpiredAt())
        .build();
  }
}
