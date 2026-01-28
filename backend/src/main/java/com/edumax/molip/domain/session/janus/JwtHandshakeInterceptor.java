package com.edumax.molip.domain.session.janus;

import com.edumax.molip.common.jwt.JwtTokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
    
    // 1. Query String에서 auth 파라미터(토큰) 추출
    // Spring의 ServletServerHttpRequest를 사용하면 getServletRequest().getParameter("auth")로 쉽게 가능하지만,
    // 여기서는 일반 ServerHttpRequest 인터페이스를 사용하므로 쿼리 파싱이 필요함.
    String query = request.getURI().getQuery();
    String token = null;

    if (query != null) {
      String[] params = query.split("&");
      for (String param : params) {
        String[] keyValue = param.split("=", 2); // Split only on first '='
        if (keyValue.length == 2 && "auth".equals(keyValue[0])) {
          token = keyValue[1];
          break;
        }
      }
    }

    // 2. 토큰 검증
    if (token != null && jwtTokenProvider.validateToken(token)) {
      // 3. 인증 정보 추출 및 세션 속성 저장 (Member ID, Role 등)
      Authentication auth = jwtTokenProvider.getAuthentication(token);
      
      // MemberUserDetails에서 memberId 추출
      if (auth.getPrincipal() instanceof com.edumax.molip.common.model.MemberUserDetails) {
          com.edumax.molip.common.model.MemberUserDetails userDetails = (com.edumax.molip.common.model.MemberUserDetails) auth.getPrincipal();
          attributes.put("memberId", userDetails.getMemberId());
          // [수정] getUsername()(이메일) 대신 getDisplayName()(실명) 사용
          attributes.put("memberName", userDetails.getDisplayName()); 
      }
      
      attributes.put("username", auth.getName()); // 이것도 저장해둠 (Email)
      
      log.info("WebSocket Handshake Authorized: username={}, memberId={}", auth.getName(), attributes.get("memberId"));
      return true;
    }

    log.warn("WebSocket Handshake Unauthorized: Token is invalid or missing");
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Exception exception) {
    // Handshake 후 처리 (필요 없음)
  }
}
