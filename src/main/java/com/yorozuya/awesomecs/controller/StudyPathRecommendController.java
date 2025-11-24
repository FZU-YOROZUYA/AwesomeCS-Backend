package com.yorozuya.awesomecs.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.response.ChatMessageResponse;
import com.yorozuya.awesomecs.service.StudyPathRecommendationsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/study_path")
@Slf4j
public class StudyPathRecommendController {

    @Resource
    private StudyPathRecommendationsService  studyPathRecommendationsService;


    @PostMapping("/")
    public Flux<String> getStudyPathRecommendations(String text) {
        Long userId = StpUtil.getLoginIdAsLong();
        return studyPathRecommendationsService.getStudyPathRecommendations(userId, text);
    }

    @DeleteMapping("/")
    public Result<Boolean> cleanMsg(){
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.buildSuccessResult(studyPathRecommendationsService.cleanChatMem(userId));
    }

    @GetMapping("/history")
    public Result<List<ChatMessageResponse>> getChatHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ChatMessageResponse> history = studyPathRecommendationsService.getChatHistoryDetailed(userId);
        return Result.buildSuccessResult(history);
    }

}
