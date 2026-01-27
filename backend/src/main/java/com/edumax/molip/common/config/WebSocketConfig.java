package com.edumax.molip.common.config;

import com.edumax.molip.domain.quiz.handler.GetRoomIdInterceptor;
import com.edumax.molip.domain.quiz.handler.VoiceForwardHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {
  private final VoiceForwardHandler voiceForwardHandler; // 생성자 주입

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(voiceForwardHandler, "/voice/{roomId}") // 경로 패턴 설정
        .addInterceptors(new GetRoomIdInterceptor()) // 인터셉터 등록
        .setAllowedOrigins("*");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/message").setAllowedOrigins("*").withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic");
  }

  @Bean
  public ServletServerContainerFactoryBean createWebSocketContainer() {
    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

    // 최대 텍스트 메시지 버퍼 크기 (필요 시)
    container.setMaxTextMessageBufferSize(512 * 1024); // 512KB

    // 최대 바이너리 메시지 버퍼 크기 (현재 발생한 문제 해결 핵심)
    // 보낸 파일이 약 115KB이므로, 넉넉하게 1MB(1024 * 1024) 정도로 설정합니다.
    container.setMaxBinaryMessageBufferSize(1024 * 1024);

    return container;
  }
}
