package com.edumax.molip.domain.auth.controller;

import com.edumax.molip.common.jwt.JwtTokenProvider;
import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.domain.auth.entity.Member;
import com.edumax.molip.domain.auth.exception.DuplicateEmailException;
import com.edumax.molip.domain.auth.model.request.LoginRequest;
import com.edumax.molip.domain.auth.model.response.LoginResponse;
import com.edumax.molip.domain.auth.service.MemberService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberService memberService;

  // jwt 테스트용
  @GetMapping("/hello")
  public String hello(@AuthenticationPrincipal MemberUserDetails user) {
    return user.getUsername();
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse res) {
    // 인증 전처리
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());

    Authentication authentication = null;
    // 인증 시도 (DetailService, passwordEncoder 구현 필수)
    try {
      authentication = authenticationManager.authenticate(authToken);
    } catch (BadCredentialsException e) {
      // 이메일/비밀번호 불일치
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new LoginResponse(null, "이메일 또는 비밀번호가 올바르지 않습니다.", null, null, null, null));
    } catch (AuthenticationException e) {
      // 그 외 Spring Security 인증 실패
      log.error("로그인 인증 오류", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      // 서버 에러
      log.error("로그인 서버 에러", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    // 인증 성공 시
    MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();

    // JWT 발급
    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    // 로그인 했으면 refresh token 새로 발급
    memberService.updateRefreshToken(userDetails.getMemberId(), refreshToken);

    ResponseCookie refreshCookie =
        ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(false) // 로컬 개발이면 false, 운영 HTTPS면 true
            .path("/")
            .maxAge(jwtTokenProvider.getRefreshTokenValiditySeconds())
            .sameSite("Lax") // 프론트/백 도메인 다르면 "None"
            .build();

    res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    LoginResponse body =
        new LoginResponse(
            accessToken,
            "로그인에 성공했습니다.",
            userDetails.getDisplayName(),
            userDetails.getMemberId(),
            userDetails.getUsername(),
            userDetails.getProfileImageUrl());

    return ResponseEntity.status(HttpStatus.OK).body(body);
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(
      @AuthenticationPrincipal MemberUserDetails member, HttpServletResponse res) {
    // 1) DB에서 refresh token 제거
    memberService.updateRefreshToken(member.getMemberId(), null);

    // 2) 클라이언트 쿠키에서 refresh token 제거
    ResponseCookie deleteCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false) // 로컬 개발이면 false, 운영 HTTPS면 true
            .path("/")
            .maxAge(0) // 즉시 만료
            .sameSite("Lax") // 프론트/백 도메인 다르면 "None"
            .build();

    res.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

    return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
  }

  @PostMapping("/register")
  public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
    try {
      Member member =
          Member.builder()
              .email(request.email)
              .passwordHash(request.password)
              .name(request.name)
              .age(request.age)
              .address(request.address)
              .gender(Member.Gender.fromDescription(request.gender))
              .role(Member.Role.USER)
              .authProvider(Member.AuthProvider.LOCAL)
              .build();

      System.out.println("member = " + member);
      memberService.registerMember(member, Member.AuthProvider.LOCAL);
      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원가입이 완료되었습니다."));
    } catch (DuplicateEmailException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "이미 존재하는 이메일입니다."));
    } catch (RuntimeException e) {
      log.error("회원가입 실패 : {}", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "회원가입에 실패했습니다: " + e.getMessage()));
    }
  }

  private record RegisterRequest(
      String email, String password, String name, Long age, String address, String gender) {}

  @DeleteMapping("member/delete")
  public ResponseEntity<Map<String, String>> deleteMember(
      @AuthenticationPrincipal MemberUserDetails member, HttpServletResponse res) {
    try {
      memberService.deleteMember(member.getMemberId());

      // 회원 탈퇴 시 쿠키도 삭제
      ResponseCookie deleteCookie =
          ResponseCookie.from("refreshToken", "")
              .httpOnly(true)
              .secure(false) // 로컬 개발이면 false, 운영 HTTPS면 true
              .path("/")
              .maxAge(0) // 즉시 만료
              .sameSite("Lax") // 프론트/백 도메인 다르면 "None"
              .build();

      res.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

      return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    } catch (RuntimeException e) {
      if (log.isDebugEnabled()) {
        log.debug("회원 탈퇴 실패 : {}", e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "회원 삭제에 실패했습니다."));
    }
  }

  @PatchMapping("/member/update")
  public ResponseEntity<Map<String, String>> updateMember(
      @AuthenticationPrincipal MemberUserDetails member, @RequestBody UpdateRequest request) {
    Member updateMember =
        Member.builder()
            .id(member.getMemberId())
            .passwordHash(request.password)
            .name(request.name)
            .age(request.age)
            .address(request.address)
            .phoneNumber(request.phoneNumber)
            .position(request.position)
            .build();
    try {
      memberService.updateMember(updateMember);
      return ResponseEntity.ok(Map.of("message", "회원 정보가 수정되었습니다."));
    } catch (RuntimeException e) {
      if (log.isDebugEnabled()) {
        log.debug("회원 정보 수정 실패 : {}", e.getMessage());
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "회원 정보 수정에 실패했습니다."));
    }
  }

  private record UpdateRequest(
      String password,
      String name,
      Long age,
      String address,
      String phoneNumber,
      String position) {}

  private record RefreshResponse(String refreshToken, String message) {}

  @GetMapping("/refresh")
  public ResponseEntity<RefreshResponse> refresh(
      @AuthenticationPrincipal MemberUserDetails userDetails,
      @CookieValue(name = "refreshToken", required = false) String refreshToken,
      HttpServletResponse res) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new RefreshResponse(null, "로그인을 해주세요")); // 쿠키 없음 → 로그인 필요
    }

    // 1) 토큰 형식/만료 검증
    if (!jwtTokenProvider.validateToken(refreshToken)
        || !jwtTokenProvider.isRefreshToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new RefreshResponse(null, "유효하지 않은 Refresh Token입니다.")); // 형식 불일치/만료
    }

    // 2) 토큰에서 username(email) 추출
    Long memberId = null;
    try {
      memberId = jwtTokenProvider.getMemberId(refreshToken);
    } catch (ExpiredJwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new RefreshResponse(null, "만료된 Refresh Token입니다.")); // 만료
    }

    // 3) DB에서 해당 유저 조회 + refresh_token 일치 여부 확인
    String tokenResult = memberService.getRefreshToken(memberId);

    if (!refreshToken.equals(tokenResult)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new RefreshResponse(null, "사용자 정보가 일치하지 않습니다.")); // 탈취/로그아웃된 토큰
    }

    // 4) 새 Access Token 발급
    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    // refresh 토큰 회전 전략: 새 refresh 발급 + DB/쿠키 교체
    memberService.updateRefreshToken(userDetails.getMemberId(), newRefreshToken);

    ResponseCookie newRefreshCookie =
        ResponseCookie.from("refreshToken", newRefreshToken)
            .httpOnly(true)
            .secure(false) // 로컬 개발이면 false, 운영 HTTPS면 true
            .path("/")
            .maxAge(jwtTokenProvider.getRefreshTokenValiditySeconds())
            .sameSite("Lax") // 프론트/백 도메인 다르면 "None"
            .build();

    RefreshResponse body = new RefreshResponse(newAccessToken, "Access Token이 재발급되었습니다.");

    res.addHeader(HttpHeaders.SET_COOKIE, newRefreshCookie.toString());

    return ResponseEntity.status(HttpStatus.OK).body(body);
  }

  //  @PostMapping("/member/profile-image")
  //  public ResponseEntity<Map<String, String>> uploadProfileImage(
  //      @AuthenticationPrincipal MemberUserDetails member,
  //      @RequestParam("file") MultipartFile file) {
  //    if (file.isEmpty()) {
  //      return ResponseEntity.badRequest().body(Map.of("message", "파일이 비어있습니다."));
  //    }
  //
  //    try {
  //      String key = memberService.uploadToS3(member.getMemberId(), file);
  //
  //      return ResponseEntity.ok(Map.of("key", key));
  //
  //    } catch (Exception e) {
  //      log.error("파일 업로드 중 오류 발생", e);
  //      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  //          .body(Map.of("message", "파일 업로드 실패: " + e.toString()));
  //    }
  //  }

  @GetMapping("/member/me")
  public ResponseEntity<Member> getMyInfo(@AuthenticationPrincipal MemberUserDetails userDetails) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    Member memberResult = memberService.findById(userDetails.getMemberId());
    if (memberResult != null) {
      memberResult.setPasswordHash(null);
    }
    return ResponseEntity.ok(memberResult);
  }
}
