package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.request.CreateInterviewRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.yorozuya.awesomecs.service.InterviewService;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    @Resource
    private InterviewService interviewService;

    @PostMapping("/create_interview")
    @SaCheckLogin
    public Result<Long> createInterview(@RequestBody CreateInterviewRequest createInterviewRequest){
        long userId = StpUtil.getLoginIdAsLong();
        Long id = interviewService.createInterview(createInterviewRequest, userId);
        return Result.buildSuccessResult(id);
    }
}
