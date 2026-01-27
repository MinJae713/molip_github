package com.edumax.molip.domain.session.janus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SessionJanusService {

    private final String janusUrl;
    private final RestTemplate restTemplate;

    public SessionJanusService(@Value("${janus.url:http://localhost:8088/janus}") String janusUrl) {
        this.janusUrl = janusUrl;
        this.restTemplate = new RestTemplate();
    }

    private String newTransaction() {
        return UUID.randomUUID().toString();
    }

    public void createRoom(long roomId, String pin, int publishers) {
        ensureRoomExists(roomId, pin, publishers);
    }

    public void destroyRoom(long roomId) {
        // 방 삭제 로직 (필요 시 구현)
        // Janus VideoRoom plugin does not have a direct "destroy room" via HTTP easily accessible 
        // without attaching a handle first, or using admin API.
        // For now, we focus on creation. Destruction might be handled by Janus or script.
    }

    @SuppressWarnings("unchecked")
    public void ensureRoomExists(long roomId, String pin, int publishers) {
        Map<String, Object> sessionResp = createSession();
        if (sessionResp == null || !sessionResp.containsKey("data")) {
            throw new RuntimeException("Failed to create Janus session");
        }
        long sessionId = ((Number) ((Map<String, Object>) sessionResp.get("data")).get("id")).longValue();
        
        Map<String, Object> attachResp = attachPlugin(sessionId);
        if (attachResp == null || !attachResp.containsKey("data")) {
             destroySession(sessionId);
             throw new RuntimeException("Failed to attach Janus plugin");
        }
        long handleId = ((Number) ((Map<String, Object>) attachResp.get("data")).get("id")).longValue();

        try {
            // Check exists
            Map<String, Object> body = new HashMap<>();
            body.put("request", "exists");
            body.put("room", roomId);
            body.put("admin_key", "videoclassroom_secret");
            Map<String, Object> messageReq = new HashMap<>();
            messageReq.put("janus", "message");
            messageReq.put("body", body);
            messageReq.put("transaction", newTransaction());
            
            Map<String, Object> existsResp = restTemplate.postForObject(janusUrl + "/" + sessionId + "/" + handleId, messageReq, Map.class);
            
            boolean exists = false;
            if (existsResp != null && "success".equals(existsResp.get("janus"))) {
                Map<String, Object> pluginData = (Map<String, Object>) existsResp.get("plugindata");
                if (pluginData != null) {
                    Map<String, Object> data = (Map<String, Object>) pluginData.get("data");
                    if (data != null && Boolean.TRUE.equals(data.get("exists"))) {
                        exists = true;
                    }
                }
            }
            
            if (!exists) {
                createRoomWithSession(sessionId, handleId, roomId, pin, publishers);
            }
        } finally {
            detachHandle(sessionId, handleId);
            destroySession(sessionId);
        }
    }

    private Map<String, Object> createSession() {
        Map<String, Object> req = new HashMap<>();
        req.put("janus", "create");
        req.put("transaction", newTransaction());
        return restTemplate.postForObject(janusUrl, req, Map.class);
    }
    
    private Map<String, Object> attachPlugin(long sessionId) {
        Map<String, Object> req = new HashMap<>();
        req.put("janus", "attach");
        req.put("plugin", "janus.plugin.videoroom");
        req.put("transaction", newTransaction());
        return restTemplate.postForObject(janusUrl + "/" + sessionId, req, Map.class);
    }
    
    private void createRoomWithSession(long sessionId, long handleId, long roomId, String pin, int publishers) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "create");
        body.put("room", roomId);
        body.put("pin", pin);
        body.put("publishers", publishers);
        body.put("record", false); // 
        body.put("rec_dir", "/opt/janus/recordings"); // 녹화 경로 설정
        body.put("secret", "secret_videoclassroom_secret"); // 방 관리 비밀번호
        body.put("admin_key", "videoclassroom_secret"); // Janus 설정에 따라 필요할 수 있음
        body.put("private", false); // 공개 방 (목록에 보이진 않음)
        
        Map<String, Object> messageReq = new HashMap<>();
        messageReq.put("janus", "message");
        messageReq.put("body", body);
        messageReq.put("transaction", newTransaction());
        
        restTemplate.postForObject(janusUrl + "/" + sessionId + "/" + handleId, messageReq, Map.class);
    }

    private void detachHandle(long sessionId, long handleId) {
        try {
            Map<String, Object> detachReq = new HashMap<>();
            detachReq.put("janus", "detach");
            detachReq.put("transaction", newTransaction());
            restTemplate.postForObject(janusUrl + "/" + sessionId + "/" + handleId, detachReq, Map.class);
        } catch (Exception ex) {
            // Ignore
        }
    }

    private void destroySession(long sessionId) {
        try {
            Map<String, Object> destroyReq = new HashMap<>();
            destroyReq.put("janus", "destroy");
            destroyReq.put("transaction", newTransaction());
            restTemplate.postForObject(janusUrl + "/" + sessionId, destroyReq, Map.class);
        } catch (Exception ex) {
            // Ignore
        }
    }
}
