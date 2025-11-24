package com.yorozuya.awesomecs.ws;

import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import com.yorozuya.awesomecs.model.domain.ConsultationMessages;
import com.yorozuya.awesomecs.model.domain.Consultations;
import com.yorozuya.awesomecs.service.ConsultationMessagesService;
import com.yorozuya.awesomecs.service.impl.ConsultationsServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConsultationWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private ConsultationsServiceImpl consultationsService;

    @Resource
    private ConsultationMessagesService consultationMessagesService;

    private final Gson gson = new Gson();

    // 存储会话：consultationId -> (userId -> session)
    private final Map<Long, Map<Long, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
        }
        String sid = (String)StpUtil.getLoginIdByToken(token);
        Long userId = 0L;
        if (sid == null || sid.isEmpty() || (userId = Long.parseLong(sid)) <= 0) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
        }
        Long consultationId = getConsultationIdFromSession(session);

        if (consultationId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing consultationId"));
            return;
        }
        // 验证用户是否参与此咨询
        Consultations consultation = consultationsService.getById(consultationId);
        if (consultation == null
                || (!consultation.getSeekerId().equals(userId) && !consultation.getExpertId().equals(userId))) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not authorized for this consultation"));
            return;
        }
        sessions.computeIfAbsent(consultationId, k -> new ConcurrentHashMap<>()).put(userId, session);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }
        String sid = (String)StpUtil.getLoginIdByToken(token);
        Long userId;
        if (sid == null || sid.isEmpty() || (userId = Long.parseLong(sid)) <= 0) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }

        Long consultationId = getConsultationIdFromSession(session);

        if (consultationId == null) {
            return;
        }

        // 解析消息
        @SuppressWarnings("unchecked")
        Map<String, Object> msgData = (Map<String, Object>) gson.fromJson(message.getPayload(), Map.class);
        String content = (String) msgData.get("content");
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // 保存消息到数据库
        // TODO: change to async or mq
        ConsultationMessages msg = new ConsultationMessages();
        msg.setConsultationId(consultationId);
        msg.setSenderId(userId);
        msg.setContent(content);
        consultationMessagesService.save(msg);

        // 广播消息给咨询中的其他用户
        Map<Long, WebSocketSession> consultationSessions = sessions.get(consultationId);
        if (consultationSessions != null) {
            Map<String, Object> broadcastMsg = Map.of(
                    "senderId", userId,
                    "content", content,
                    "sendTime", msg.getCreatedAt().toString());
            String jsonMsg = gson.toJson(broadcastMsg);

            for (Map.Entry<Long, WebSocketSession> entry : consultationSessions.entrySet()) {
                if (!entry.getKey().equals(userId)) {
                    try {
                        entry.getValue().sendMessage(new TextMessage(jsonMsg));
                    } catch (IOException e) {
                        log.error("Failed to send message to user {}", entry.getKey(), e);
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[ConsultationWebSocketHandler]: afterConnectionClosed");
        String token = getTokenFromSession(session);
        if (token != null && StpUtil.isLogin(token)) {
            Long userId = StpUtil.getLoginIdAsLong();
            Long consultationId = getConsultationIdFromSession(session);

            if (consultationId != null) {
                Map<Long, WebSocketSession> consultationSessions = sessions.get(consultationId);
                if (consultationSessions != null) {
                    consultationSessions.remove(userId);
                    if (consultationSessions.isEmpty()) {
                        sessions.remove(consultationId);
                    }
                }
                log.info("User {} disconnected from consultation {}", userId, consultationId);
            }
        }
    }

    private String getTokenFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        log.info("[getTokenFromSession]: query is {}", query);
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

    private Long getConsultationIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length >= 3 && "ws".equals(parts[1]) && "consultation".equals(parts[2])) {
            try {
                return Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
