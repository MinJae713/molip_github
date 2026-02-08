package com.edumax.molip.common.config;

import com.edumax.molip.common.jwt.JwtTokenProvider;
import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.common.model.RedisProfile;
import com.edumax.molip.common.util.JacksonUtil;
import com.edumax.molip.domain.quiz.handler.SttWebSocketInterceptor;
import com.edumax.molip.domain.quiz.handler.VoiceForwardHandler;
import com.edumax.molip.domain.session.handler.EmotionForwardHandler;
import com.edumax.molip.domain.session.handler.EmotionWebSocketInterceptor;
import com.edumax.molip.domain.session.janus.JwtHandshakeInterceptor;
import com.edumax.molip.domain.session.janus.SessionJanusHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Slf4j
@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {
  private final StringRedisTemplate redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  private final VoiceForwardHandler voiceForwardHandler; // 생성자 주입
  private final EmotionForwardHandler emotionForwardHandler;
  private final SessionJanusHandler sessionJanusHandler;

  private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(voiceForwardHandler, "/voice/{roomId}") // 경로 패턴 설정
        .addInterceptors(new SttWebSocketInterceptor()) // 인터셉터 등록
        .setAllowedOriginPatterns("*");

    registry
        .addHandler(emotionForwardHandler, "/emotion/*") // 경로 패턴 설정
        .addInterceptors(new EmotionWebSocketInterceptor()) // 인터셉터 등록
        .setAllowedOriginPatterns("*");

    registry
        .addHandler(sessionJanusHandler, "/ws/janus")
        .addInterceptors(jwtHandshakeInterceptor)
        .setAllowedOrigins("*");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/message").setAllowedOriginPatterns("*").withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic");
  }

  private String PROFILE_KEY_FORMAT = "chatting:profile:%s";

  private String getProfileKey(String email) {
    return PROFILE_KEY_FORMAT.formatted(email);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        new ChannelInterceptor() {
          @Override
          public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor acc =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            // connect 단계에서 인증 헤더 주입
            if (StompCommand.CONNECT.equals(acc.getCommand())) {
              try {
                String auth = acc.getFirstNativeHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                  String token = auth.substring(7);
                  String email = jwtTokenProvider.getUsername(token);

                  String redisKey = getProfileKey(email);
                  String profileResult = redisTemplate.opsForValue().get(redisKey);

                  Authentication authentication = null;
                  if (profileResult != null) {
                    RedisProfile redisProfile =
                        JacksonUtil.fromJson(profileResult, RedisProfile.class);
                    MemberUserDetails userDetails =
                        new MemberUserDetails(
                            redisProfile.getEmail(),
                            redisProfile.getName(),
                            redisProfile.getMemberId(),
                            redisProfile.getProfileImageUrl());
                    authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                  } else {
                    authentication = jwtTokenProvider.getAuthentication(token);
                    MemberUserDetails userDetails =
                        (MemberUserDetails) authentication.getPrincipal();
                    RedisProfile profile =
                        RedisProfile.builder()
                            .email(userDetails.getUsername())
                            .memberId(userDetails.getMemberId())
                            .name(userDetails.getDisplayName())
                            .profileImageUrl(userDetails.getProfileImageUrl())
                            .build();
                    redisTemplate.opsForValue().set(redisKey, JacksonUtil.toJson(profile));
                  }
                  acc.setUser(authentication);
                }
              } catch (Exception e) {
                log.error("STOMP Authentication fail: {}", e.getMessage());
                return null; // 연결 거부
              }
            }
            return message;
          }
        });
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
