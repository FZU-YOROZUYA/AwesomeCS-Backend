package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.AwesomeCsApplication;
import com.yorozuya.awesomecs.service.impl.UsersServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = AwesomeCsApplication.class)
public class UserServiceTest {
    @Resource
    private UsersServiceImpl usersService;

    @Test
    @Transactional
    public void testLogin() {
        String username = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "password123";
        String phone = "13800138000";
        String avatar = "http://example.com/avatar.png";
        String targetJob = "Backend Engineer";
        List<String> techs = List.of("Java", "Spring");
        String bio = "Hello World";

        // 注册用户
        long userId = usersService.registerUser(username, password, phone, avatar, targetJob, techs, bio);
        Assertions.assertTrue(userId > 0, "注册失败，ID应大于0");

        // 尝试登录
        long loginId = usersService.loginByUsername(username, password);
        Assertions.assertEquals(userId, loginId, "登录返回的ID应与注册ID一致");

        // 尝试错误密码登录
        Assertions.assertThrows(Exception.class, () -> {
            usersService.loginByUsername(username, "wrongPassword");
        }, "错误密码应抛出异常");
    }
}
