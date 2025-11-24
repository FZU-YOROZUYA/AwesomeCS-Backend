package com.yorozuya.awesomecs.ws_config;

import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InterviewWebSocketHandler extends BinaryWebSocketHandler {

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

        try {

            // TODO
            session.sendMessage(null);

        } catch (Exception e) {
            log.error("Error processing interview message for user {}", userId, e);
            // 可以发送错误消息给前端
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
