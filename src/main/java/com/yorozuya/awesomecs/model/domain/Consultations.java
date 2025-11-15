package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 付费咨询表
 * @TableName consultations
 */
@TableName(value ="consultations")
@Data
public class Consultations implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 专家（提供咨询的用户）ID
     */
    private Long expertId;

    /**
     * 咨询者ID
     */
    private Long seekerId;

    /**
     * 状态：0-待支付，1-已预约，2-已完成，3-已取消
     */
    private Integer status;


    /**
     * 
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}