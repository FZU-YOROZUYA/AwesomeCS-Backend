package com.yorozuya.awesomecs.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.service.StudyPathRecommendationsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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

}
