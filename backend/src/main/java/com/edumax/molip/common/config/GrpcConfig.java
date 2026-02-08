package com.edumax.molip.common.config;

import com.edumax.molip.grpc.emotion.EmotionServiceGrpc;
import com.edumax.molip.grpc.qna.QnaServiceGrpc;
import com.edumax.molip.grpc.quiz.QuizServiceGrpc;
import com.edumax.molip.grpc.stt.STTServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

  @Bean
  public QuizServiceGrpc.QuizServiceBlockingStub quizServiceBlockingStub(
      GrpcChannelFactory channelFactory) {
    return QuizServiceGrpc.newBlockingStub(channelFactory.createChannel("quiz-service"));
  }

  @Bean
  public STTServiceGrpc.STTServiceStub sttServiceStub(GrpcChannelFactory channelFactory) {
    return STTServiceGrpc.newStub(channelFactory.createChannel("stt-service"));
  }

  @Bean
  public EmotionServiceGrpc.EmotionServiceStub emotionServiceStub(
      GrpcChannelFactory channelFactory) {
    return EmotionServiceGrpc.newStub(channelFactory.createChannel("emotion-service"));
  }

  @Bean
  public QnaServiceGrpc.QnaServiceBlockingStub qnaServiceBlockingStub(
      GrpcChannelFactory channelFactory) {
    return QnaServiceGrpc.newBlockingStub(channelFactory.createChannel("qna-service"));
  }
}
