package com.yorozuya.awesomecs.ws;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Resource
    private ConsultationWebSocketHandler consultationWebSocketHandler;

    @Resource
    private InterviewWebSocketHandler interviewWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(consultationWebSocketHandler, "/ws/consultation/**")
                .setAllowedOrigins("*");
        registry.addHandler(interviewWebSocketHandler, "/ws/interview/**")
                .setAllowedOrigins("*");
    }
}
