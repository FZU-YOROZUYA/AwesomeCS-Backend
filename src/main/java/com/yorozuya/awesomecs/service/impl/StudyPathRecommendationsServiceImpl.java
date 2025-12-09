package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.model.domain.StudyPathRecommendations;
import com.yorozuya.awesomecs.repository.mapper.StudyPathRecommendationsMapper;
import com.yorozuya.awesomecs.service.StudyPathRecommendationsService;
import com.yorozuya.awesomecs.service.ai.Tools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import com.yorozuya.awesomecs.model.response.ChatMessageResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author wjc28
* @description 针对表【study_path_recommendations(AI学习路径推荐表)】的数据库操作Service实现
* @createDate 2025-11-01 15:55:02
*/
@Service
public class StudyPathRecommendationsServiceImpl extends ServiceImpl<StudyPathRecommendationsMapper, StudyPathRecommendations>
    implements StudyPathRecommendationsService {

    @Resource
    private DeepSeekChatModel chatModel;

    @Resource
    private Tools tools;

    private final ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(128).build();

    private final String SYS_PROMPT = """
            你是一个 IT 行业的学习路径推荐师，你的工作是根据用户的目标岗位和掌握的技术栈去推荐用户接下来最合适的学习规划。
            """;

    @Override
    public Flux<String> getStudyPathRecommendations(Long userId, String text) {
        String sUserId = String.valueOf(userId);
        initChatMemory(sUserId);
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(sUserId).build())  // 添加 advisor 来自动处理内存
                .build();

        return chatClient
                .prompt(new Prompt(
                        text
                ))
                .tools(tools)
                .toolContext(Map.of("userId", sUserId))
                .stream()
                .content();
    }



    @Override
    public boolean cleanChatMem(Long userId) {
        chatMemory.clear(String.valueOf(userId));
        return true;
    }

    @Override
    public List<ChatMessageResponse> getChatHistoryDetailed(Long userId) {
        String sUserId = String.valueOf(userId);
        initChatMemory(sUserId);
        List<Message> messages = chatMemory.get(sUserId);
        List<ChatMessageResponse> history = new ArrayList<>();
        for (Message message : messages) {
            if (!(message instanceof SystemMessage)) {
                if (message instanceof UserMessage) {
                    history.add(new ChatMessageResponse("user", message.getText()));
                } else if (message instanceof AssistantMessage) {
                    history.add(new ChatMessageResponse("assistant", message.getText()));
                }
            }
        }
        return history;
    }

    private void initChatMemory(String userId) {
        List<Message> messages = chatMemory.get(userId);
        if (messages.isEmpty()) {
            String userInfo = "用户的 id 为 " + userId;
            chatMemory.add(userId, new SystemMessage(SYS_PROMPT + userInfo));
            chatMemory.add(userId, new AssistantMessage("你好！我是你的学习路径规划师。"));
        }
        return;
    }

}




