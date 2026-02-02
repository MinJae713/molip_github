package com.edumax.molip.domain.session.janus;

import com.edumax.molip.common.jwt.JwtTokenProvider;
import com.edumax.molip.common.model.MemberUserDetails;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
  private final StringRedisTemplate redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  String SESSION_JOIN_FORMAT = "session:join:session-id:%s";

  String getJoinKey(String sessionId) {
    return String.format(SESSION_JOIN_FORMAT, sessionId);
  }

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes)
      throws Exception {

    // [수정] 1. UriComponentsBuilder를 사용한 안전하고 간결한 쿼리 파싱
    // 기존의 split("&") 방식보다 인코딩 처리 및 예외 상황에 훨씬 견고합니다.
    MultiValueMap<String, String> queryParams =
        UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

    String token = queryParams.getFirst("auth");
    String sessionId = queryParams.getFirst("session");

    // [수정] 2. sessionId 누락 검증 로직 추가
    // sessionId가 null이면 getJoinKey에서 "null" 문자열이 결합되므로 미리 차단해야 합니다.
    if (sessionId == null || sessionId.trim().isEmpty()) {
      log.warn("WebSocket Handshake Failed: sessionId is missing in query parameters");
      return false;
    }

    // 2. 토큰 검증
    if (token != null && jwtTokenProvider.validateToken(token)) {
      Authentication auth = jwtTokenProvider.getAuthentication(token);

      // [수정] Java 16+ Pattern Matching 도입 (형변환 간소화)
      if (auth.getPrincipal() instanceof MemberUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        String memberName = userDetails.getDisplayName();

        attributes.put("memberId", memberId);
        attributes.put("memberName", memberName);
        attributes.put("username", auth.getName()); // Email

        String joinKey = getJoinKey(sessionId);
        String joinValue = memberId + "|" + memberName;

        // [수정] 3. Redis 데이터 추가 및 만료 시간(TTL) 설정
        // 리스트에 데이터를 넣고, 해당 키가 무한히 증식하지 않도록 만료 시간을 설정합니다. (예: 24시간)
        redisTemplate.opsForSet().add(joinKey, joinValue);
        redisTemplate.expire(joinKey, Duration.ofHours(24));
      }

      log.info(
          "WebSocket Handshake Authorized: username={}, memberId={}, sessionId={}",
          auth.getName(),
          attributes.get("memberId"),
          sessionId);
      return true;
    }

    log.warn("WebSocket Handshake Unauthorized: Token is invalid or missing");
    return false;
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {
    // Handshake 후 처리 (필요 없음)
  }
}
