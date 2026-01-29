package com.edumax.molip.domain.session.handler;

import com.edumax.molip.domain.session.dto.EmotionSaveEvent;
import com.edumax.molip.domain.session.dto.response.StudentEmotionMessage;
import com.edumax.molip.grpc.emotion.EmotionRequest;
import com.edumax.molip.grpc.emotion.EmotionResponse;
import com.edumax.molip.grpc.emotion.EmotionServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Slf4j
@Component
public class EmotionForwardHandler extends BinaryWebSocketHandler {
  private final SimpMessagingTemplate messagingTemplate;
  private final ApplicationEventPublisher eventPublisher;
  private final EmotionServiceGrpc.EmotionServiceStub emotionServiceStub;
  private final EmotionManagerRegistry emotionManagerRegistry; // 1. 필드 추가

  private final Map<String, StreamObserver<EmotionRequest>> grpcStreamMap =
      new ConcurrentHashMap<>();

  // 2. 생성자 파라미터에 추가
  public EmotionForwardHandler(
      @Lazy SimpMessagingTemplate messagingTemplate,
      ApplicationEventPublisher eventPublisher,
      EmotionServiceGrpc.EmotionServiceStub emotionServiceStub,
      @Lazy EmotionManagerRegistry emotionManagerRegistry) { // 주입
    this.messagingTemplate = messagingTemplate;
    this.eventPublisher = eventPublisher;
    this.emotionServiceStub = emotionServiceStub;
    this.emotionManagerRegistry = emotionManagerRegistry;
  }

  // 연결할때 호출
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    // 1. 웹소켓 연결 시 gRPC 스트림을 오픈합니다.
    StreamObserver<EmotionResponse> responseObserver =
        new StreamObserver<>() {
          @Override
          public void onNext(EmotionResponse response) {
            log.info(
                "Emotion 응답 수신: {} - {} - {}",
                response.getTimeline(),
                response.getEmotion1(),
                response.getConfidence1());
            Map<String, Object> attributes = session.getAttributes();

            String sessionId = (String) attributes.get("sessionId");
            Long memberId = (Long) attributes.get("memberId");
            int timeline = response.getTimeline();

            eventPublisher.publishEvent(
                new EmotionSaveEvent(
                    memberId,
                    sessionId,
                    timeline,
                    response.getEmotion1(),
                    response.getConfidence1(),
                    response.getEmotion2(),
                    response.getConfidence2(),
                    response.getEmotion3(),
                    response.getConfidence3()));

            StudentEmotionMessage message =
                new StudentEmotionMessage(
                    "emotion",
                    timeline,
                    response.getEmotion1(),
                    response.getConfidence1(),
                    response.getEmotion2(),
                    response.getConfidence2(),
                    response.getEmotion3(),
                    response.getConfidence3());

            float[] emotionValueList = new float[7];
            emotionValueList[EmotionManager.getEmotionIndex(response.getEmotion1())] =
                response.getConfidence1();
            emotionValueList[EmotionManager.getEmotionIndex(response.getEmotion2())] =
                response.getConfidence2();
            emotionValueList[EmotionManager.getEmotionIndex(response.getEmotion3())] =
                response.getConfidence3();

            emotionManagerRegistry.getManager(sessionId).insertEmotion(timeline, emotionValueList);

            messagingTemplate.convertAndSend(
                "/topic/analysis/" + sessionId + "/" + memberId, message);
          }

          @Override
          public void onError(Throwable t) {
            log.error("Emotion 스트리밍 에러", t);
          }

          @Override
          public void onCompleted() {
            log.info("Emotion 스트리밍 완료");
          }
        };

    // 비동기 gRPC 스트림 시작 및 맵에 보관
    StreamObserver<EmotionRequest> requestObserver =
        emotionServiceStub.analyzeEmotion(responseObserver);
    grpcStreamMap.put(getGrpcStreamKey(session), requestObserver);
  }

  private String getGrpcStreamKey(WebSocketSession session) {
    String sessionId = (String) session.getAttributes().get("sessionId");
    String memberId = String.valueOf(session.getAttributes().get("memberId"));
    return sessionId + "-" + memberId;
  }

  // 데이터 수신시 호출
  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
    String grpcStreamKey = getGrpcStreamKey(session);
    StreamObserver<EmotionRequest> requestObserver = grpcStreamMap.get(grpcStreamKey);

    if (requestObserver == null) {
      log.error("gRPC stream not found for sessionId: {}", grpcStreamKey);
      return;
    }

    ByteBuffer buffer = message.getPayload();

    // 2. 최소 4바이트(int)는 있어야 timeline을 읽을 수 있음
    if (buffer.remaining() < 4) {
      log.error("Payload too short to contain timeline (min 4 bytes)");
      return;
    }

    // 3. 앞의 4바이트를 읽어서 timeline으로 추출
    // ByteBuffer.getInt()는 4바이트를 읽고 포인터를 4만큼 이동시킵니다.
    int timeline = buffer.getInt();

    byte[] imageBytes = new byte[buffer.remaining()];
    buffer.get(imageBytes);

    EmotionRequest request =
        EmotionRequest.newBuilder()
            .setImageData(ByteString.copyFrom(imageBytes))
            .setMemberId(String.valueOf(session.getAttributes().get("memberId")))
            .setTimeline(timeline)
            .build();

    requestObserver.onNext(request);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    // 3. 웹소켓 연결이 끊기면 gRPC onCompleted를 호출하여 스트림을 닫습니다.
    StreamObserver<EmotionRequest> requestObserver =
        grpcStreamMap.remove(getGrpcStreamKey(session));
    if (requestObserver != null) {
      requestObserver.onCompleted();
      System.out.println("웹소켓 종료로 인한 gRPC 연결 종료");
    }
  }
}
