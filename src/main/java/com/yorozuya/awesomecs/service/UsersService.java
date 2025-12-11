package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.model.domain.Users;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author wjc28
 * @description 针对表【users(用户基础信息表)】的数据库操作Service
 * @createDate 2025-11-01 15:55:02
 */
public interface UsersService extends IService<Users> {
        long registerUser(
                        String username,
                        String password,
                        String avatarUrl,
                        String targetJob,
                        List<String> techs,
                        String bio);

        long loginByUsername(
                        String username,
                        String password);

        /**
         * 更新用户头像
         * 
         * @param id        用户ID
         * @param avatarUrl 头像URL
         * @return 是否更新成功
         */
        boolean updateAvatar(Long id, String avatarUrl);

        /**
         * 更新用户个人资料（bio, techs, targetJob）
         * 
         * @param id        用户ID
         * @param bio       个人简介
         * @param techs     技术栈
         * @param targetJob 目标岗位
         * @return 是否更新成功
         */
        boolean updateProfile(Long id, String bio, List<String> techs, String targetJob);

        /**
         * 修改密码
         * */
        boolean updatePwd(Long id,String oldPwd,String newPwd);
}
