package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章评论表
 * @TableName comments
 */
@TableName(value ="comments")
@Data
public class Comments implements Serializable {
    @TableId
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}