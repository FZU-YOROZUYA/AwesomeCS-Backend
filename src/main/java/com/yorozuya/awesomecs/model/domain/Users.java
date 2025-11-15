package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户基础信息表
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 手机号（唯一标识）
     */
    private String phone;

    /**
     * 
     */
    private String nickname;

    private String password;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 存储兴趣、技术栈等扩展信息
     */
    private Object userData;

    /**
     * 账户状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public Users(Long id, String phone, String nickname, String password, String avatar, String bio, Object userData, Integer status) {
        this.id = id;
        this.phone = phone;
        this.nickname = nickname;
        this.password = password;
        this.avatar = avatar;
        this.bio = bio;
        this.userData = userData;
        this.status = status;
    }
}