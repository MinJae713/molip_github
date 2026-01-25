package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.grpc.connection.ConnectionServiceGrpc;
import com.edumax.molip.grpc.connection.PingRequest;
import com.edumax.molip.grpc.connection.PongResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class ConnectionService extends ConnectionServiceGrpc.ConnectionServiceImplBase {
  @Override
  public void ping(PingRequest request, StreamObserver<PongResponse> responseObserver) {
    if (request.getMessage().isEmpty()) {
      // 메시지가 비어있을 경우 INVALID_ARGUMENT 에러를 반환
      responseObserver.onError(
          Status.INVALID_ARGUMENT.withDescription("Message cannot be empty").asRuntimeException());
      return;
    }

    PongResponse response =
        PongResponse.newBuilder().setReply("Echo: " + request.getMessage()).setStatus(true).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
