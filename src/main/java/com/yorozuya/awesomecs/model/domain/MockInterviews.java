package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 模拟面试记录表
 * @TableName mock_interviews
 */
@TableName(value ="mock_interviews")
@Data
public class MockInterviews implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private Long userId;

    /**
     * 面试领域
     * */
    private String domain;

    /**
     * 面试风格
     * */
    private String style;



    /**
     * 面试录音/录像存储URL
     */
    private String recordingUrl;


    /**
     * 
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}