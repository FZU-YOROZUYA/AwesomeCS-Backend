package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.model.domain.StudyPathRecommendations;
import com.yorozuya.awesomecs.repository.mapper.StudyPathRecommendationsMapper;
import com.yorozuya.awesomecs.service.StudyPathRecommendationsService;
import com.yorozuya.awesomecs.service.ai.Tools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
        List<Message> messages = chatMemory.get(sUserId);
        if (messages.isEmpty()) {
            chatMemory.add(sUserId, new SystemMessage(SYS_PROMPT));
        }
        chatMemory.add(sUserId, new UserMessage(sUserId));

        return ChatClient.create(chatModel)
                .prompt(new Prompt(
                        chatMemory.get(sUserId)
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

}




