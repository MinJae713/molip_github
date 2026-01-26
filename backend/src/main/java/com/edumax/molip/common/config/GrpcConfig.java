package com.edumax.molip.common.config;

import com.edumax.molip.grpc.connection.ConnectionServiceGrpc;
import com.edumax.molip.grpc.stt.STTServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

  @Bean
  public ConnectionServiceGrpc.ConnectionServiceBlockingStub connectionServiceStub(
      GrpcChannelFactory channelFactory) {
    return ConnectionServiceGrpc.newBlockingStub(channelFactory.createChannel("connection-client"));
  }

  @Bean
  public STTServiceGrpc.STTServiceStub sttServiceStub(GrpcChannelFactory channelFactory) {
    return STTServiceGrpc.newStub(channelFactory.createChannel("stt-service"));
  }
}
