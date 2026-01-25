package com.edumax.molip.domain.quiz.service;

import com.edumax.molip.grpc.connection.ConnectionServiceGrpc;
import com.edumax.molip.grpc.connection.PingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GrpcClientService {
  private final ConnectionServiceGrpc.ConnectionServiceBlockingStub stub;

  public String sendPing(String msg) {
    PingRequest request = PingRequest.newBuilder().setMessage(msg).build();
    return stub.ping(request).getReply();
  }
}
