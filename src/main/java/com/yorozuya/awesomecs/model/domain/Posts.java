package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 博客文章表
 * @TableName posts
 */
@TableName(value ="posts")
@Data
public class Posts implements Serializable {
    @TableId
    private Long id;
    private Long userId;
    private String category;
    private String tags;
    private String title;
    private String content;
    private String summary;
    private Integer status;
    private Integer viewCount;
    private Date createdAt;
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}