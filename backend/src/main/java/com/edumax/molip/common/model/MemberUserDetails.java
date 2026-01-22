package com.edumax.molip.common.model;

import com.edumax.molip.auth.entity.Member;
import java.util.Set;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberUserDetails implements UserDetails {
  private String username;
  private String password;
  private Set<SimpleGrantedAuthority> authorities;
  // extra fields
  private String displayName;
  private Long memberId;
  private String profileImageUrl;

  public MemberUserDetails(Member member) {
    this.username = member.getEmail();
    this.password = member.getPasswordHash();
    this.displayName = member.getName();
    this.memberId = member.getId();
    this.authorities = Set.of(new SimpleGrantedAuthority(member.getRole().toString()));
    this.profileImageUrl = member.getProfileImageUrl();
  }
}
