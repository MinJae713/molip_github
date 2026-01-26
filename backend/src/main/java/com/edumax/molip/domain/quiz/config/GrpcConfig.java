package com.edumax.molip.domain.quiz.config;

import com.edumax.molip.grpc.connection.ConnectionServiceGrpc;
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
}
