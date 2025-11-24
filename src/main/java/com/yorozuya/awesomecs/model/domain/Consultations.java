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
    @TableId
    private Long id;
    private Long expertId;
    private Long seekerId;
    private Integer status;
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}