package com.edumax.molip.domain.quiz.handler;

import com.edumax.molip.domain.quiz.dto.SttSaveEvent;
import com.edumax.molip.grpc.stt.STTRequest;
import com.edumax.molip.grpc.stt.STTResponse;
import com.edumax.molip.grpc.stt.STTServiceGrpc;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceForwardHandler extends BinaryWebSocketHandler {
  private final ApplicationEventPublisher eventPublisher;
  private final STTServiceGrpc.STTServiceStub sttServiceStub;
  private final Map<String, StreamObserver<STTRequest>> grpcStreamMap = new ConcurrentHashMap<>();

  // 연결할때 호출
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {

    // 1. 웹소켓 연결 시 gRPC 스트림을 오픈합니다.
    StreamObserver<STTResponse> responseObserver =
        new StreamObserver<>() {
          @Override
          public void onNext(STTResponse response) {
            log.info(
                "STT 응답 수신: {} - {} - {}",
                response.getStatus(),
                response.getTimeline(),
                response.getText());

            eventPublisher.publishEvent(
                new SttSaveEvent(
                    (String) session.getAttributes().get("sessionId"),
                    response.getText(),
                    response.getTimeline()));
          }

          @Override
          public void onError(Throwable t) {
            log.error("STT 스트리밍 에러", t);
          }

          @Override
          public void onCompleted() {
            log.info("STT 스트리밍 완료");
          }
        };

    // 비동기 gRPC 스트림 시작 및 맵에 보관
    StreamObserver<STTRequest> requestObserver = sttServiceStub.sendSTT(responseObserver);
    grpcStreamMap.put((String) session.getAttributes().get("sessionId"), requestObserver);
  }

  // 데이터 수신시 호출
  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
    String sessionId = (String) session.getAttributes().get("sessionId");
    StreamObserver<STTRequest> requestObserver = grpcStreamMap.get(sessionId);

    if (requestObserver == null) {
      log.error("gRPC stream not found for sessionId: {}", sessionId);
      return;
    }

    ByteBuffer buffer = message.getPayload();

    // 2. 최소 4바이트(int)는 있어야 timeline을 읽을 수 있음
    if (buffer.remaining() < 4) {
      log.error("Payload too short to contain timeline (min 4 bytes)");
      return;
    }

    int timeline = buffer.getInt();

    byte[] voiceBytes = new byte[buffer.remaining()];
    buffer.get(voiceBytes);

    STTRequest request =
        STTRequest.newBuilder()
            .setSessionId(sessionId)
            .setTimeline(timeline) // 클라이언트가 보낸 값 사용
            .setAudioData(ByteString.copyFrom(voiceBytes))
            .build();

    requestObserver.onNext(request);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    // 3. 웹소켓 연결이 끊기면 gRPC onCompleted를 호출하여 스트림을 닫습니다.
    StreamObserver<STTRequest> requestObserver =
        grpcStreamMap.remove((String) session.getAttributes().get("sessionId"));
    if (requestObserver != null) {
      requestObserver.onCompleted();
      System.out.println("웹소켓 종료로 인한 gRPC 연결 종료");
    }
  }
}
