package com.yorozuya.awesomecs.ws;

import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import com.yorozuya.awesomecs.service.ai.ModelClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InterviewWebSocketHandler extends BinaryWebSocketHandler {

    @Resource
    private ModelClient  modelClient;

    private final Gson gson = new Gson();

    // 存储会话：userId -> session
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null || !StpUtil.isLogin(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }

        Long userId = StpUtil.getLoginIdAsLong();
        sessions.put(userId, session);
        log.info("User {} connected to interview chat", userId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null || !StpUtil.isLogin(token)) {
            return;
        }

        Long userId = StpUtil.getLoginIdAsLong();
        ByteBuffer buffer = message.getPayload();
        byte[] audioData = new byte[buffer.remaining()];
        buffer.get(audioData);

        try
        {
            String input = modelClient.audio2Text(new ByteArrayInputStream(audioData), userId);
            String answer = modelClient.chat(input, userId);
            byte[] rst = modelClient.textToSpeech(answer);
            session.sendMessage(new BinaryMessage(rst));

        } catch (Exception e) {
            log.error("Error processing interview message for user {}", userId, e);
            session.sendMessage(new TextMessage("{\"error\": \"Processing failed\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String token = getTokenFromSession(session);
        if (token != null && StpUtil.isLogin(token)) {
            Long userId = StpUtil.getLoginIdAsLong();
            sessions.remove(userId);
            log.info("User {} disconnected from interview chat", userId);
        }
    }

    private String getTokenFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}
