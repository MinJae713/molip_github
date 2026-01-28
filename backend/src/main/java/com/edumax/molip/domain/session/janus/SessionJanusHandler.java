package com.edumax.molip.domain.session.janus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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

import com.edumax.molip.domain.course.repository.CourseApplyRepository;
import com.edumax.molip.domain.session.entity.Session;
import com.edumax.molip.domain.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;

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
    
    log.info("Client Connected: {} [memberId={}, username={}]", clientSession.getId(), memberId, username);

    pendingMessages.put(clientSession.getId(), new ConcurrentLinkedQueue<>());

    WebSocketClient janusClient = new StandardWebSocketClient();
    WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    headers.setSecWebSocketProtocol(Collections.singletonList("janus-protocol"));

    janusClient.execute(new TextWebSocketHandler() {
      @Override
      public void handleTextMessage(WebSocketSession janusSession, TextMessage message) throws Exception {
        handleJanusMessage(janusSession, clientSession, message);
      }

      @Override
      public void afterConnectionClosed(WebSocketSession janusSession, CloseStatus status) throws Exception {
        if (clientSession.isOpen()) {
          clientSession.close(status);
        }
        cleanup(clientSession.getId(), janusSession.getId());
      }
    }, headers, URI.create(janusWsUrl)).whenComplete((janusSession, ex) -> {
      if (ex != null) {
        log.error("Failed to connect to Janus", ex);
        try {
          clientSession.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) { /* ignore */ }
        pendingMessages.remove(clientSession.getId());
      } else {
        clientToJanusMap.put(clientSession.getId(), janusSession);
        janusToClientMap.put(janusSession.getId(), clientSession);

        Queue<TextMessage> queue = pendingMessages.remove(clientSession.getId());
        if (queue != null) {
          while (!queue.isEmpty()) {
            TextMessage msg = queue.poll();
            try {
              janusSession.sendMessage(msg);
            } catch (IOException e) {
              log.error("Failed to send buffered message", e);
            }
          }
        }
      }
    });
  }

  @Override
  protected void handleTextMessage(WebSocketSession clientSession, TextMessage message) throws Exception {
    WebSocketSession janusSession = clientToJanusMap.get(clientSession.getId());

    // [권한 검사 및 Signal 변조 - Client -> Janus]
    String payload = message.getPayload();
    
    // [LOG] Client -> Janus (Raw)
    log.info("[Client->Janus] Raw: {}", payload);
    
    // (1) join 요청 가로채기
    if (payload.contains("\"join\"")) {
        try {
            Map<String, Object> json = objectMapper.readValue(payload, new TypeReference<>() {});
            Map<String, Object> body = (Map<String, Object>) json.get("body");
            
            if (body != null && "join".equals(body.get("request")) && body.containsKey("room")) {
                Long janusRoomId = ((Number) body.get("room")).longValue();
                Long memberId = (Long) clientSession.getAttributes().get("memberId");
                String memberName = (String) clientSession.getAttributes().get("memberName");
                
                if (memberId != null) {
                    Session session = sessionRepository.findByJanusRoomId(janusRoomId)
                        .orElse(null);
                        
                    if (session != null) {
                        Long instructorId = session.getCourse().getLeaner().getMember().getId();
                        String role;
                        String displayName;
                        
                        if (instructorId.equals(memberId)) {
                            role = "TEACHER";
                            displayName = "[선생] " + memberName;
                        } else {
                            // 수강생인지 확인 (CourseApply 테이블 조회)
                            boolean isStudent = courseApplyRepository.existsByMemberIdAndCourseId(memberId, session.getCourse().getCourseId());
                            if (isStudent) {
                                role = "STUDENT";
                                displayName = "[학생] " + memberName;
                            } else {
                                log.warn("Client {} has NO PERMISSION for Room {}", memberId, janusRoomId);
                                clientSession.close(CloseStatus.POLICY_VIOLATION);
                                return;
                            }
                        }
                        
                        // 세션 속성에 저장
                        clientSession.getAttributes().put("role", role);
                        clientSession.getAttributes().put("displayName", displayName);
                        clientSession.getAttributes().put("courseId", session.getCourse().getCourseId());
                        clientSession.getAttributes().put("sessionId", session.getSessionId());
                        
                        // [변조] Display Name 강제 설정
                        // PIN은 클라이언트가 보낸 것 그대로 사용 (검증은 Janus에게 위임)
                        body.put("display", displayName);
                        
                        // 다시 JSON으로 변환하여 Janus로 전송
                        payload = objectMapper.writeValueAsString(json);
                        
                        log.info("Client {} joining Room {} as {} ({})", memberId, janusRoomId, role, displayName);
                    } else {
                        log.warn("Room {} not found in DB", janusRoomId);
                        clientSession.close(CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error intercepting join message", e);
        }
    }
    
    // (2) publish 요청 가로채기 (강제 display name 적용 & 녹화 설정)
    else if (payload.contains("\"publish\"") || payload.contains("\"configure\"")) {
        try {
            Map<String, Object> json = objectMapper.readValue(payload, new TypeReference<>() {});
            Map<String, Object> body = (Map<String, Object>) json.get("body");
            
            if (body != null && ("publish".equals(body.get("request")) || "configure".equals(body.get("request")))) {
                
                // 1. 사용자 정보 가져오기
                String role = (String) clientSession.getAttributes().get("role");
                String displayName = (String) clientSession.getAttributes().get("displayName");
                Long courseId = (Long) clientSession.getAttributes().get("courseId");
                Long sessionId = (Long) clientSession.getAttributes().get("sessionId");
                
                if (role != null && displayName != null) {
                    
                    // 2. Display Name 강제 적용 (위변조 방지)
                    // - publish 요청이면 무조건 삽입
                    // - configure 요청에 display가 포함되어 있다면 무조건 덮어쓰기
                    boolean isPublish = "publish".equals(body.get("request"));
                    if (isPublish || body.containsKey("display")) {
                        body.put("display", displayName); 
                    }
                    
                    // 3. 녹화 설정 (강사: 강제 녹화, 학생: 녹화 금지)
                    if ("TEACHER".equals(role) && isPublish) {
                         // 강사는 무조건 녹화 시작
                         String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
                         String timestamp = String.valueOf(System.currentTimeMillis());
                         
                         if (courseId == null) courseId = 0L;
                         if (sessionId == null) sessionId = 0L;
                         
                         String filename = String.format("%s_%d_%d_%s", uuid, courseId, sessionId, timestamp);
                         
                         // 프론트에서 보낸 값이 있더라도 덮어씀 (Map.put은 덮어쓰기임)
                         body.put("record", true);
                         body.put("filename", filename);
                         body.put("rec_dir", "/opt/janus/recordings");
                         
                         log.info("Enforcing recording for TEACHER: {} in /opt/janus/recordings", filename);
                         
                    } else if ("STUDENT".equals(role) && isPublish) {
                        // 학생은 절대 녹화 금지
                        // 프론트가 record: true를 보내도 여기서 false로 덮어씌워짐
                        body.put("record", false);
                        body.remove("filename");
                        body.remove("rec_dir");
                    }
                    
                    // 변경된 내용 적용
                    payload = objectMapper.writeValueAsString(json);
                }
            }
        } catch (Exception e) {
            log.error("Error intercepting publish message", e);
        }
    }

    // [LOG] Client -> Janus (Sent)
    log.info("[Client->Janus] Sending: {}", payload);
    
    if (janusSession != null && janusSession.isOpen()) {
      janusSession.sendMessage(new TextMessage(payload));
    } else {
      Queue<TextMessage> queue = pendingMessages.get(clientSession.getId());
      if (queue != null) {
        queue.add(new TextMessage(payload));
      }
    }
  }

  private void handleJanusMessage(WebSocketSession janusSession, WebSocketSession clientSession, TextMessage message) throws Exception {
    String payload = message.getPayload();
    
    // [LOG] Janus -> Client (Raw)
    log.info("[Janus->Client] Raw: {}", payload);

    if (!payload.contains("publishers") && !payload.contains("videoroom")) {
      clientSession.sendMessage(message);
      return;
    }

    try {
      Map<String, Object> json = objectMapper.readValue(payload, new TypeReference<>() {});
      String role = (String) clientSession.getAttributes().get("role");

      // 학생에게는 다른 학생의 캠을 숨기는 필터링 로직
      // [정책] 학생은 "선생님 화면([선생]...)"만 볼 수 있음. 다른 "[학생]..."은 숨김.
      if ("STUDENT".equals(role)) {
        if (json.containsKey("publishers")) {
          List<Map<String, Object>> publishers = (List<Map<String, Object>>) json.get("publishers");
          List<Map<String, Object>> filtered = new ArrayList<>();
          for (Map<String, Object> pub : publishers) {
            String display = (String) pub.get("display");
            // [선생] 으로 시작하는 것만 허용
            if (display != null && display.startsWith("[선생]")) {
              filtered.add(pub);
            }
          }
          json.put("publishers", filtered);
          payload = objectMapper.writeValueAsString(json);
        }
        
        if (json.containsKey("plugindata")) {
            Map<String, Object> pluginData = (Map<String, Object>) json.get("plugindata");
            if (pluginData != null && pluginData.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) pluginData.get("data");
                if (data.containsKey("publishers")) {
                    List<Map<String, Object>> publishers = (List<Map<String, Object>>) data.get("publishers");
                    List<Map<String, Object>> filtered = new ArrayList<>();
                    for (Map<String, Object> pub : publishers) {
                        String display = (String) pub.get("display");
                        if (display != null && display.startsWith("[선생]")) {
                            filtered.add(pub);
                        }
                    }
                    data.put("publishers", filtered);
                    payload = objectMapper.writeValueAsString(json);
                }
            }
        }
      }

      // [LOG] Janus -> Client (Sent)
      log.info("[Janus->Client] Sending: {}", payload);
      clientSession.sendMessage(new TextMessage(payload));
    } catch (Exception e) {
      log.error("Error filtering msg", e);
      clientSession.sendMessage(message);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    WebSocketSession janusSession = clientToJanusMap.remove(session.getId());
    if (janusSession != null) {
      janusToClientMap.remove(janusSession.getId());
      if (janusSession.isOpen()) {
        janusSession.close();
      }
    }
  }

  private void cleanup(String clientId, String janusId) {
    clientToJanusMap.remove(clientId);
    janusToClientMap.remove(janusId);
  }
}
