package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import com.yorozuya.awesomecs.model.request.AvatarUpdateRequest;
import com.yorozuya.awesomecs.model.request.ProfileUpdateRequest;
import com.yorozuya.awesomecs.service.UsersService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Resource
    private UsersService usersService;

    @PostMapping("/register")
    @SaIgnore
    public Result<String> register(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestParam("avatar") String avatarUrl,
            @RequestParam("target_job") String targetJob,
            @RequestParam("techs") List<String> techs,
            @RequestParam("bio") String bio) {
        HashMap<String, String> rst = new HashMap<>();
        long id = usersService.registerUser(
                username,
                password,
                phone,
                avatarUrl,
                targetJob,
                techs,
                bio);
        return Result.buildSuccessResult("ok");
    }

    @PostMapping("/login/username")
    @SaIgnore
    public Result<Map<String, String>> login(@RequestParam("username") String username,
            @RequestParam("password") String password) {
        long id = usersService.loginByUsername(username, password);
        StpUtil.login(id);
        Map<String, String> rst = new HashMap<>();
        rst.put("token", StpUtil.getTokenValue());
        return Result.buildSuccessResult(rst);
    }

    @PostMapping("/avatar/update")
    @SaCheckLogin
    public Result<Object> updateAvatar(@RequestBody AvatarUpdateRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        usersService.updateAvatar(userId, req.getAvatar());
        return Result.buildSuccessResult(null);
    }

    @PostMapping("/profile/update")
    @SaCheckLogin
    public Result<Object> updateProfile(@RequestBody ProfileUpdateRequest req, @RequestHeader("Authorization") String token) {
        Long userId = StpUtil.getLoginIdAsLong();
        usersService.updateProfile(userId, req.getBio(), req.getTechs(), req.getTargetJob());
        return Result.buildSuccessResult(null);
    }

    @PostMapping("/password/update")
    @SaCheckLogin
    public Result<Object> updatePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword")String newPassword,
            @RequestHeader("Authorization") String token
        ) {
        Long userId = StpUtil.getLoginIdAsLong();
        usersService.updatePwd(userId, oldPassword, newPassword);
        return Result.buildSuccessResult(null);
    }
}