package com.edumax.molip.domain.session.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmotionManagerRegistry {
  // SessionId를 키로 하여 강의별 매니저 보관
  private final Map<String, EmotionManager> managers = new ConcurrentHashMap<>();
  private final SimpMessagingTemplate messagingTemplate; // 웹소켓 전송을 위한 템플릿

  // 매니저 가져오기 (없으면 새로 생성)
  public EmotionManager getManager(String sessionId) {
    return managers.computeIfAbsent(sessionId, id -> new EmotionManager(messagingTemplate));
  }

  // 강의 종료 시 호출하여 제거 및 자원 해제
  public void removeManager(String sessionId) {
    EmotionManager manager = managers.remove(sessionId);
    if (manager != null) {
      manager.endLecture(); // 스케줄러 종료 메서드
    }
  }
}
