package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.domain.MockInterviews;
import com.yorozuya.awesomecs.model.request.CreateInterviewRequest;
import com.yorozuya.awesomecs.service.InterviewService;
import com.yorozuya.awesomecs.service.ai.ModelClient;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/interview")
@CrossOrigin
public class InterviewController {

    @Resource
    private InterviewService interviewService;

    @Resource
    private ModelClient modelClient;

    // chat memory per interview
    private ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(1024).build();

    @PostMapping("/create_interview")
    @SaCheckLogin
    public Result<String> createInterview(@RequestBody CreateInterviewRequest createInterviewRequest){
        long userId = StpUtil.getLoginIdAsLong();
        // validate domain and style
        String domain = createInterviewRequest.getDomain();
        String style = createInterviewRequest.getStyle();

        // allowed values
        java.util.Set<String> allowedDomains = java.util.Set.of("后端", "算法", "移动开发", "前端", "嵌入式");
        java.util.Set<String> allowedStyles = java.util.Set.of("压力面", "友好面", "专业面", "引导面");

        if (domain == null || !allowedDomains.contains(domain)) {
            return Result.buildErrorResult("Invalid domain value. Allowed: 后端, 算法, 移动开发, 前端, 嵌入式");
        }
        if (style == null || !allowedStyles.contains(style)) {
            return Result.buildErrorResult("Invalid style value. Allowed: 压力面, 友好面, 专业面, 引导面");
        }

        Long id = interviewService.createInterview(createInterviewRequest, userId);
        return Result.buildSuccessResult(String.valueOf(id));
    }

    /**
     * HTTP audio endpoint: POST /api/interview/{id}/audio
     * consumes: application/octet-stream (raw audio bytes)
     * returns: audio/wav binary (AI reply)
     */
    @PostMapping(path = "/{id}/audio", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @SaCheckLogin
    public ResponseEntity<byte[]> handleAudio(@PathVariable("id") Long id, HttpServletRequest request) {
        long userId = StpUtil.getLoginIdAsLong();

        MockInterviews interview = interviewService.getById(id);
        if (interview == null) {
            String err = "{\"error\": \"Invalid interviewId\"}";
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(err.getBytes());
        }

        String sInterviewId = String.valueOf(id);
        // ensure system prompt + memory
        if (chatMemory.get(sInterviewId).isEmpty()) {
            chatMemory.add(sInterviewId, new SystemMessage(
                    generateSystemPrompt(
                            interview, String.valueOf(userId)
                    )
            ));
        }

        try (InputStream in = request.getInputStream()) {
            String input = modelClient.audio2Text(in, String.valueOf(userId));
            log.info(input);
            chatMemory.add(sInterviewId, new UserMessage(input));
            String answer = modelClient.chat(chatMemory.get(sInterviewId), input, String.valueOf(userId));
            chatMemory.add(sInterviewId, new AssistantMessage(answer));
            String hexAudio = modelClient.textToSpeech(answer);
            byte[] rst = Hex.decodeHex(hexAudio);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/wav"));
            return new ResponseEntity<>(rst, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error processing interview audio for user {} interview {}", userId, id, e);
            String err = "{\"error\": \"Processing failed\"}";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(err.getBytes());
        }
    }

    private String generateSystemPrompt(MockInterviews interview, String userId) {
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

        prompt.append("你的回答应该只包括对用户的询问，一次只问一个问题。用户的 userId 为" + userId);

        return prompt.toString();
    }
}
