package com.yorozuya.awesomecs.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import com.yorozuya.awesomecs.model.domain.Users;
import com.yorozuya.awesomecs.repository.mapper.UsersMapper;
import com.yorozuya.awesomecs.service.UsersService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author wjc28
 * @description 针对表【users(用户基础信息表)】的数据库操作Service实现
 * @createDate 2025-11-01 15:55:02
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
        implements UsersService {
    @Resource
    private UsersMapper usersMapper;

    @Override
    public long registerUser(String username, String password, String phone, String avatarUrl, String targetJob,
            List<String> techs, String bio) {
        long uid = IdUtil.getSnowflakeNextId();
        String afterPassword = DigestUtil.md5Hex(password);
        HashMap<String, Object> userInterest = new HashMap<>();
        userInterest.put("targetJob", targetJob);
        userInterest.put("techs", techs);
        String userInterestJson = new Gson().toJson(userInterest);
        boolean success = this.save(new Users(
                uid,
                phone,
                username,
                afterPassword,
                avatarUrl,
                bio,
                userInterestJson,
                1));
        return success ? uid : -1L;
    }

    @Override
    public long loginByUsername(String username, String password) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getNickname, username);
        Users one = getOne(wrapper);
        if (one == null) {
            throw new BusinessException(Constants.ResponseCode.NO_OBJECT);
        }
        if (!DigestUtil.md5Hex(password).equals(one.getPassword())) {
            throw new BusinessException(Constants.ResponseCode.PASSWORD_WRONG);
        }
        return one.getId();
    }

    @Override
    public boolean updateAvatar(Long id, String avatarUrl) {
        Users user = this.getById(id);
        if (user == null) {
            throw new BusinessException(Constants.ResponseCode.NO_OBJECT);
        }
        user.setAvatar(avatarUrl);
        return this.updateById(user);
    }

    @Override
    public boolean updateProfile(Long id, String bio, List<String> techs, String targetJob) {
        Users user = this.getById(id);
        if (user == null) {
            throw new BusinessException(Constants.ResponseCode.NO_OBJECT);
        }
        user.setBio(bio);
        // userData 存储 JSON，保持原有结构
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("targetJob", targetJob);
        userData.put("techs", techs);
        String userDataJson = new Gson().toJson(userData);
        user.setUserData(userDataJson);
        return this.updateById(user);
    }
}
