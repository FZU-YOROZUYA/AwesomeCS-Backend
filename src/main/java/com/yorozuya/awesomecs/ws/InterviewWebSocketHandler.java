package com.yorozuya.awesomecs.ws;

import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import com.yorozuya.awesomecs.model.domain.MockInterviews;
import com.yorozuya.awesomecs.service.InterviewService;
import com.yorozuya.awesomecs.service.ai.ModelClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InterviewWebSocketHandler extends BinaryWebSocketHandler {

    @Resource
    private ModelClient modelClient;

    @Resource
    private InterviewService interviewService;

    private ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(1024).build();
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null || token.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }
        Object loginIdByToken = StpUtil.getLoginIdByToken(token);
        if (loginIdByToken == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
            return;
        }
        String userId = loginIdByToken.toString();
        session.getAttributes().put("userId", userId);
        Long interviewId = getInterviewIdFromSession(session);
        if (interviewId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing interviewId"));
            return;
        }
        MockInterviews interview = interviewService.getById(interviewId);
        if (interview == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid interviewId"));
            return;
        }
        String sInterviewId = String.valueOf(interviewId);
        session.getAttributes().put("interview", interview);
        chatMemory.clear(sInterviewId);
        String systemPrompt = generateSystemPrompt(interview);
        session.getAttributes().put("systemPrompt", systemPrompt);
        chatMemory.add(sInterviewId, new SystemMessage(systemPrompt));

        sessions.put(interviewId, session);

        session.setBinaryMessageSizeLimit(15 * 1024 * 1024);
        log.info("User {} connected to interview chat for interview {}", userId, interviewId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String userId = session.getAttributes().get("userId").toString();
        ByteBuffer buffer = message.getPayload();
        byte[] audioData = new byte[buffer.remaining()];
        buffer.get(audioData);

        try
        {
            String input = modelClient.audio2Text(new ByteArrayInputStream(audioData), userId);
            MockInterviews interview = (MockInterviews) session.getAttributes().get("interview");

            String answer = modelClient.chat(chatMemory.get(String.valueOf(interview.getId())), input, userId);
            String base64Audio = modelClient.textToSpeech(answer);
            byte[] rst = Base64.getDecoder().decode(base64Audio);
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
            MockInterviews interview = (MockInterviews) session.getAttributes().get("interview");
            sessions.remove(interview.getId());
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

    private Long getInterviewIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length >= 3 && "ws".equals(parts[1]) && "interview".equals(parts[2])) {
            try {
                return Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String generateSystemPrompt(MockInterviews interview) {
        String domain = interview.getDomain();
        String style = interview.getStyle();

        StringBuilder prompt = new StringBuilder("你是一个 IT 行业的面试专家。");

        if (domain != null) {
            prompt.append(String.format("你的面试领域是 %s。", domain));
            if ("后端".equals(domain)) prompt.append("请侧重于 Java, Spring Boot, MySQL, Redis, 分布式系统等技术栈。");
            else if ("算法".equals(domain)) prompt.append("请侧重于数据结构, 算法设计, 机器学习, 深度学习等知识。");
            else if ("前端".equals(domain)) prompt.append("请侧重于 HTML, CSS, JavaScript, Vue/React, 浏览器原理等。");
            else if ("嵌入式".equals(domain)) prompt.append("请侧重于 C/C++, 操作系统, 驱动开发, 硬件接口等。");
            else if ("移动开发".equals(domain)) prompt.append("请侧重于 Android/iOS 开发, 移动端性能优化, 混合开发等。");
        }

        if (style != null) {
            if ("压力面".equals(style)) prompt.append("你需要不断追问，质疑候选人的回答，考察其抗压能力和深度。语气要严厉，节奏要快。不要轻易满意。");
            else if ("友好面".equals(style)) prompt.append("你需要态度和蔼，多给予鼓励，引导候选人回答。语气要亲切。如果候选人答不上来，可以给一些提示。");
            else if ("专业面".equals(style)) prompt.append("你需要专注于技术细节和底层原理，考察候选人的技术深度。语气要客观、专业。");
            else if ("引导面".equals(style)) prompt.append("当候选人卡壳时，你需要循循善诱，给出提示，帮助候选人理清思路。注重考察解决问题的思路。");
        }

        prompt.append("你的回答应该只包括对用户的询问，一次只问一个问题。");

        return prompt.toString();
    }
}
