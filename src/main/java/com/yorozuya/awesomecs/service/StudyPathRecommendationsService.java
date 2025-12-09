package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.model.domain.StudyPathRecommendations;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yorozuya.awesomecs.model.response.ChatMessageResponse;
import reactor.core.publisher.Flux;
import java.util.List;

/**
* @author wjc28
* @description 针对表【study_path_recommendations(AI学习路径推荐表)】的数据库操作Service
* @createDate 2025-11-01 15:55:02
*/
public interface StudyPathRecommendationsService extends IService<StudyPathRecommendations> {
    Flux<String> getStudyPathRecommendations(Long userId, String text);

    boolean cleanChatMem(Long userId);

    List<ChatMessageResponse> getChatHistoryDetailed(Long userId);
}

