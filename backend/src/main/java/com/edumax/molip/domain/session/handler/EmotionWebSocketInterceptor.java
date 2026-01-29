package com.edumax.molip.domain.session.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

public class EmotionWebSocketInterceptor extends HttpSessionHandshakeInterceptor {
  @Override
  public boolean beforeHandshake( // 이거는 한번 설정해놓으면 세션 유지동안 값이 고정됨
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes)
      throws Exception {

    // URL 예: ws://localhost:8080/voice/room/123
    String path = request.getURI().getPath();
    String sessionId = path.substring(path.lastIndexOf('/') + 1);
    attributes.put("sessionId", sessionId);

    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpRequest = servletRequest.getServletRequest();

      String memberId = httpRequest.getParameter("member-id"); // memberId도 필요하시죠?

      if (memberId != null) {
        attributes.put("memberId", Long.parseLong(memberId));
      }
    }

    // 부모의 기능을 유지하기 위해 super 호출 (HTTP 세션 연동이 필요 없다면 true도 무방)
    return super.beforeHandshake(request, response, wsHandler, attributes);
  }
}
