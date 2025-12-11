package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.request.AvatarUpdateRequest;
import com.yorozuya.awesomecs.model.request.ProfileUpdateRequest;
import com.yorozuya.awesomecs.model.request.UpdatePasswordRequest;
import com.yorozuya.awesomecs.model.response.UserProfileResponse;
import com.yorozuya.awesomecs.model.domain.Users;
import com.yorozuya.awesomecs.service.UsersService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
@CrossOrigin
public class UserController {

    @Resource
    private UsersService usersService;

    @PostMapping("/register")
    @SaIgnore
    public Result<String> register(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("avatar") String avatarUrl,
            @RequestParam("target_job") String targetJob,
            @RequestParam("techs") List<String> techs,
            @RequestParam("bio") String bio) {
        usersService.registerUser(
                username,
                password,
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

    @GetMapping("/profile")
    @SaCheckLogin
    public Result<UserProfileResponse> getProfile() {
        Long id = StpUtil.getLoginIdAsLong();
        Users user = usersService.getById(id);
        if (user == null) {
            return Result.buildErrorResult("用户不存在");
        }

        String nickname = user.getNickname();
        String avatar = user.getAvatar();
        String bio = user.getBio();

        // userData 存储为 JSON 字符串（由 UsersServiceImpl 使用 Gson 写入），解析后提取 targetJob 和 techs
        String targetJob = null;
        List<String> techs = null;
        Object ud = user.getUserData();
        if (ud != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.lang.reflect.Type mapType = new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>() {}.getType();
                java.util.Map<String, Object> map = gson.fromJson(ud.toString(), mapType);
                if (map != null) {
                    Object tj = map.get("targetJob");
                    if (tj != null) targetJob = tj.toString();
                    Object t = map.get("techs");
                    if (t instanceof List) {
                        techs = new ArrayList<>();
                        for (Object elt : (List<?>) t) {
                            if (elt != null) techs.add(elt.toString());
                        }
                    }else if (t instanceof String) {
                        techs =  new ArrayList<>();
                        techs.add((String) t);
                    }
                }
            } catch (Exception e) {
                // ignore parse errors, 保持 null
            }
        }

        UserProfileResponse resp = new UserProfileResponse(id, nickname, avatar, bio, targetJob, techs);
        return Result.buildSuccessResult(resp);
    }

    @PostMapping("/password/update")
    @SaCheckLogin
    public Result<Object> updatePassword(
            @RequestBody UpdatePasswordRequest updatePasswordRequest,
            @RequestHeader("Authorization") String token
        ) {
        Long userId = StpUtil.getLoginIdAsLong();
        usersService.updatePwd(
                userId,
                updatePasswordRequest.getOldPassword(),
                updatePasswordRequest.getNewPassword());
        return Result.buildSuccessResult(null);
    }

    @DeleteMapping
    @SaCheckLogin
    public Result<Object> logout(@RequestHeader("Authorization") String token) {
        StpUtil.logout();
        return Result.buildSuccessResult(null);
    }

}