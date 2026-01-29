package com.edumax.molip.domain.session.janus;

import com.edumax.molip.domain.course.repository.CourseApplyRepository;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.session.repository.SessionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionJanusHandler extends TextWebSocketHandler implements SubProtocolCapable {

  @Value("${janus.ws.url:ws://localhost:8188}")
  private String janusWsUrl;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final SessionRepository sessionRepository;
  private final CourseApplyRepository courseApplyRepository;

  private final Map<String, WebSocketSession> clientToJanusMap = new ConcurrentHashMap<>();
  private final Map<String, WebSocketSession> janusToClientMap = new ConcurrentHashMap<>();
  private final Map<String, Queue<TextMessage>> pendingMessages = new ConcurrentHashMap<>();

  @Override
  public List<String> getSubProtocols() {
    return Collections.singletonList("janus-protocol");
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession clientSession) throws Exception {
    Long memberId = (Long) clientSession.getAttributes().get("memberId");
    String username = (String) clientSession.getAttributes().get("username");

    log.info(
        "Client Connected: {} [memberId={}, username={}]",
        clientSession.getId(),
        memberId,
        username);

    pendingMessages.put(clientSession.getId(), new ConcurrentLinkedQueue<>());

    WebSocketClient janusClient = new StandardWebSocketClient();
    WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    headers.setSecWebSocketProtocol(Collections.singletonList("janus-protocol"));

    janusClient
        .execute(
            new TextWebSocketHandler() {
              @Override
              public void handleTextMessage(WebSocketSession janusSession, TextMessage message)
                  throws Exception {
                handleJanusMessage(clientSession, message);
              }

              @Override
              public void afterConnectionClosed(WebSocketSession janusSession, CloseStatus status)
                  throws Exception {
                try {
                  if (clientSession.isOpen()) {
                    clientSession.close(status);
                  }
                } finally {
                  cleanup(clientSession.getId(), janusSession.getId());
                  if (janusSession.isOpen()) {
                    janusSession.close();
                  }
                }
              }
            },
            headers,
            URI.create(janusWsUrl))
        .whenComplete(
            (janusSession, ex) -> {
              if (ex != null) {
                log.error("Failed to connect to Janus", ex);
                try {
                  clientSession.close(CloseStatus.SERVER_ERROR);
                } catch (IOException e) {
                  log.error("Failed to close client session", e);
                }
                pendingMessages.remove(clientSession.getId());
                if (janusSession != null) {
                  try {
                    janusSession.close();
                  } catch (IOException closeEx) {
                    log.warn("Failed to close janusSession", closeEx);
                  }
                }
              } else {
                clientToJanusMap.put(clientSession.getId(), janusSession);
                janusToClientMap.put(janusSession.getId(), clientSession);

                Queue<TextMessage> queue = pendingMessages.remove(clientSession.getId());
                if (queue != null) {
                  while (!queue.isEmpty()) {
                    TextMessage msg = queue.poll();
                    try {
                      if (janusSession.isOpen()) {
                        janusSession.sendMessage(msg);
                      }
                    } catch (IOException e) {
                      log.error("Failed to send buffered message", e);
                    }
                  }
                }
              }
            });
  }

  @Override
  protected void handleTextMessage(WebSocketSession clientSession, TextMessage message)
      throws Exception {
    // [PMD] 맵에서 꺼낸 세션은 전역 관리되므로 여기서 닫지 않음
    WebSocketSession janusSession = clientToJanusMap.get(clientSession.getId()); // NOPMD

    try {
      String payload = message.getPayload();
      log.info("[Client->Janus] Raw: {}", payload);

      if (payload.contains("\"join\"")) {
        // join 요청인 경우 세션을 직접 넘겨서 내부에서 sendMessage 하도록 수정
        handleJoinRequest(clientSession, janusSession, payload);
        return;
      }

      if (payload.contains("\"publish\"") || payload.contains("\"configure\"")) {
        payload = handlePublishRequest(clientSession, payload);
      }

      if (janusSession != null && janusSession.isOpen()) {
        janusSession.sendMessage(new TextMessage(payload));
      } else if (janusSession == null) {
        Queue<TextMessage> queue = pendingMessages.get(clientSession.getId());
        if (queue != null) {
          queue.add(new TextMessage(payload));
        }
      }
    } catch (Exception e) {
      log.error("Error in handleTextMessage", e);
      throw e;
    }
  }

  private void handleJoinRequest(
      WebSocketSession clientSession, WebSocketSession janusSession, String payload)
      throws Exception { // NOPMD
    try {
      Map<String, Object> json = objectMapper.readValue(payload, new TypeReference<>() {});
      Map<String, Object> body = (Map<String, Object>) json.get("body");

      if (body != null && "join".equals(body.get("request")) && body.containsKey("room")) {
        Long janusRoomId = ((Number) body.get("room")).longValue();
        Long memberId = (Long) clientSession.getAttributes().get("memberId");
        String memberName = (String) clientSession.getAttributes().get("memberName");

        if (memberId != null) {
          Session session = sessionRepository.findByJanusRoomId(janusRoomId).orElse(null);

          if (session != null) {
            Long instructorId = session.getCourse().getLeaner().getMember().getId();
            String role = instructorId.equals(memberId) ? "TEACHER" : "STUDENT";
            String displayName =
                instructorId.equals(memberId) ? "[선생] " + memberName : "[학생] " + memberName;

            if ("STUDENT".equals(role)
                && !courseApplyRepository.existsByMemberIdAndCourseId(
                    memberId, session.getCourse().getCourseId())) {
              clientSession.close(CloseStatus.POLICY_VIOLATION);
              return;
            }

            clientSession.getAttributes().put("role", role);
            clientSession.getAttributes().put("displayName", displayName);
            clientSession.getAttributes().put("courseId", session.getCourse().getCourseId());
            clientSession.getAttributes().put("sessionId", session.getSessionId());

            body.put("display", displayName);
            String newPayload = objectMapper.writeValueAsString(json);

            if (janusSession != null && janusSession.isOpen()) {
              janusSession.sendMessage(new TextMessage(newPayload));
            }
          } else {
            clientSession.close(CloseStatus.POLICY_VIOLATION);
          }
        }
      }
    } catch (Exception e) {
      log.error("Error join msg", e);
    }
  }

  private String handlePublishRequest(WebSocketSession clientSession, String payload) {
    try {
      Map<String, Object> json = objectMapper.readValue(payload, new TypeReference<>() {});
      Map<String, Object> body = (Map<String, Object>) json.get("body");

      if (body != null
          && ("publish".equals(body.get("request")) || "configure".equals(body.get("request")))) {
        String role = (String) clientSession.getAttributes().get("role");
        String displayName = (String) clientSession.getAttributes().get("displayName");
        Long courseId = (Long) clientSession.getAttributes().get("courseId");
        Long sessionId = (Long) clientSession.getAttributes().get("sessionId");

        if (role != null && displayName != null) {
          body.put("display", displayName);
          if ("TEACHER".equals(role) && "publish".equals(body.get("request"))) {
            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
            String filename =
                String.format(
                    "%s_%d_%d_%s",
                    uuid,
                    courseId != null ? courseId : 0L,
                    sessionId != null ? sessionId : 0L,
                    String.valueOf(System.currentTimeMillis()));
            body.put("record", true);
            body.put("filename", filename);
            body.put("rec_dir", "/opt/janus/recordings");
          } else if ("STUDENT".equals(role)) {
            body.put("record", false);
          }
          return objectMapper.writeValueAsString(json);
        }
      }
    } catch (Exception e) {
      log.error("Error publish msg", e);
    }
    return payload;
  }

  private void handleJanusMessage(WebSocketSession clientSession, TextMessage message)
      throws Exception {
    String payload = message.getPayload();
    if (clientSession.isOpen()) {
      clientSession.sendMessage(new TextMessage(payload));
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    // [PMD] 맵에서 제거하고 닫는 절차는 여기서 완결됨
    WebSocketSession janusSession = clientToJanusMap.remove(session.getId()); // NOPMD

    try {
      if (janusSession != null) {
        janusToClientMap.remove(janusSession.getId());
        if (janusSession.isOpen()) {
          janusSession.close(status);
        }
      }
    } finally {
      cleanup(session.getId(), janusSession != null ? janusSession.getId() : null);
    }
  }

  private void cleanup(String clientId, String janusId) {
    if (clientId != null) {
      clientToJanusMap.remove(clientId);
      pendingMessages.remove(clientId);
    }
    if (janusId != null) {
      janusToClientMap.remove(janusId);
    }
  }
}
