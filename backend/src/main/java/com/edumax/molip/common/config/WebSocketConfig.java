package com.edumax.molip.common.config;

import com.edumax.molip.common.jwt.JwtTokenProvider;
import com.edumax.molip.common.model.MemberUserDetails;
import com.edumax.molip.common.model.RedisProfile;
import com.edumax.molip.common.util.JacksonUtil;
import com.edumax.molip.domain.quiz.handler.GetRoomIdInterceptor;
import com.edumax.molip.domain.quiz.handler.VoiceForwardHandler;
import com.edumax.molip.domain.session.janus.SessionJanusHandler; // import 추가
import com.edumax.molip.domain.session.janus.JwtHandshakeInterceptor; // import 추가 (패키지 경로 확인 필요)

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
    
    // 핸들러 주입
    private final VoiceForwardHandler voiceForwardHandler;
    private final SessionJanusHandler sessionJanusHandler; // 추가됨
    
    // 인터셉터 주입
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor; // 추가됨

    // =====================================================================
    // 1. WebSocketHandler 등록 (Raw WebSocket: Voice & Janus)
    // =====================================================================
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 기존: Voice Handler
        registry.addHandler(voiceForwardHandler, "/voice/{roomId}")
            .addInterceptors(new GetRoomIdInterceptor())
            .setAllowedOriginPatterns("*");

        // 추가됨: Janus Handler (/ws/janus)
        registry.addHandler(sessionJanusHandler, "/ws/janus")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOrigins("*"); // 또는 setAllowedOriginPatterns("*") 사용 가능
    }

    // =====================================================================
    // 2. STOMP 설정
    // =====================================================================
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/message")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

    // =====================================================================
    // 3. STOMP 인증 인터셉터 (ChannelInterceptor)
    // =====================================================================
    private String PROFILE_KEY_FORMAT = "chatting:profile:%s";

    private String getProfileKey(String email) {
        return PROFILE_KEY_FORMAT.formatted(email);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
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
                                RedisProfile redisProfile = JacksonUtil.fromJson(profileResult, RedisProfile.class);
                                MemberUserDetails userDetails = new MemberUserDetails(
                                    redisProfile.getEmail(),
                                    redisProfile.getName(),
                                    redisProfile.getMemberId(),
                                    redisProfile.getProfileImageUrl()
                                );
                                authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            } else {
                                authentication = jwtTokenProvider.getAuthentication(token);
                                MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();
                                RedisProfile profile = RedisProfile.builder()
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
                        return null;
                    }
                }
                return message;
            }
        });
    }

    // =====================================================================
    // 4. WebSocket 컨테이너 설정 (버퍼 사이즈 등)
    // =====================================================================
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        
        container.setMaxTextMessageBufferSize(512 * 1024); // 512KB
        
        // 기존 1MB 유지 (JanusConfig의 512KB 요구사항도 충분히 수용함)
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 1MB

        return container;
    }
}