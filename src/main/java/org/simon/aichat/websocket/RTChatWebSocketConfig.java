package org.simon.aichat.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RTChatWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册websocket处理器和拦截器
        registry.addHandler(webSocketHandler(), "/rtchat")
                .addInterceptors(webSocketHandleInterceptor()).setAllowedOrigins("*");
    }

    @Bean
    public RTChatWebSocketHandler webSocketHandler() {
        return new RTChatWebSocketHandler();
    }

    @Bean
    public RTChatWebSocketHandleInterceptor webSocketHandleInterceptor() {
        return new RTChatWebSocketHandleInterceptor();
    }
}
