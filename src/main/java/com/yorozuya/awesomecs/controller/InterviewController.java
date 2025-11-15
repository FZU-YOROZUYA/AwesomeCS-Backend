package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    @GetMapping("/ws-token")
    @SaCheckLogin
    public Result<String> getWsToken() {
        // 生成 WebSocket 连接 token
        String token = StpUtil.getTokenValue();
        return Result.buildSuccessResult(token);
    }

    // TODO: 添加其他面试相关的 REST 接口
    // 例如：开始面试、结束面试、获取面试历史等
}
