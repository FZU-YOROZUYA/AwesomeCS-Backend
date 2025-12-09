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

    // 存储用户会话：一个用户只允许一个 websocket 会话
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
        }
        String sid = (String)StpUtil.getLoginIdByToken(token);
        long userId = 0L;
        if (sid == null || sid.isEmpty() || (userId = Long.parseLong(sid)) <= 0) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
        }
        // 用户只允许一个 websocket 会话，如已有则关闭旧连接
        WebSocketSession old = userSessions.put(userId, session);
        if (old != null && old.isOpen() && old != session) {
            try {
                old.close(CloseStatus.NORMAL.withReason("Replaced by new session"));
            } catch (Exception ignore) {
            }
        }
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

        // 解析消息体：必须包含 consultation_id, to_id, content
        @SuppressWarnings("unchecked")
        Map<String, Object> msgData = (Map<String, Object>) gson.fromJson(message.getPayload(), Map.class);
        Long consultationId = toLong(msgData.get("consultation_id"));
        Long toId = toLong(msgData.get("to_id"));
        String content = (String) msgData.get("content");

        if (consultationId == null || toId == null || content == null || content.trim().isEmpty()) {
            session.close(CloseStatus.BAD_DATA.withReason("Invalid message payload"));
            return;
        }

        // 校验咨询存在且双方匹配
        Consultations consultation = consultationsService.getById(consultationId);
        if (consultation == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Consultation not found"));
            return;
        }
        if (consultation.getStatus() != 1){
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Consultation not found"));
            return;
        }
        boolean participantsMatch =
                (consultation.getExpertId().equals(userId) && consultation.getSeekerId().equals(toId))
                        || (consultation.getSeekerId().equals(userId) && consultation.getExpertId().equals(toId));
        if (!participantsMatch) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not authorized for this consultation"));
            return;
        }

        // 保存消息到数据库
        ConsultationMessages msg = new ConsultationMessages();
        msg.setConsultationId(consultationId);
        msg.setSenderId(userId);
        msg.setContent(content);
        consultationMessagesService.save(msg);

        // 转发给目标用户（如果在线）
        WebSocketSession target = userSessions.get(toId);
        if (target != null && target.isOpen()) {
            Map<String, Object> forwardMsg = Map.of(
                    "consultation_id", consultationId,
                    "sender_id", userId,
                    "to_id", toId,
                    "content", content,
                    "send_time", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : new Date().toString()
            );
            try {
                target.sendMessage(new TextMessage(gson.toJson(forwardMsg)));
            } catch (IOException e) {
                log.error("Failed to send message to user {}", toId, e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[ConsultationWebSocketHandler]: afterConnectionClosed");
        String token = getTokenFromSession(session);
        if (token != null && StpUtil.isLogin(token)) {
            Long userId = StpUtil.getLoginIdAsLong();
            userSessions.remove(userId, session);
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

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }
}
