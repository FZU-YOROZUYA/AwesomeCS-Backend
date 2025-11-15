package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "consultation_relation")
@Data
public class ConsultationRelation implements Serializable {
    private Long id;
    private Long userId;
    private Double price;
    private String domains;
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
