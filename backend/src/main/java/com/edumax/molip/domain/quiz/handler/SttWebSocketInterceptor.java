package com.edumax.molip.domain.quiz.handler;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

public class SttWebSocketInterceptor extends HttpSessionHandshakeInterceptor {
  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes)
      throws Exception {

    // URL 예: ws://localhost:8080/voice/room/123
    String path = request.getURI().getPath();
    String roomId = path.substring(path.lastIndexOf('/') + 1);
    attributes.put("sessionId", roomId);

    return super.beforeHandshake(request, response, wsHandler, attributes);
  }
}
