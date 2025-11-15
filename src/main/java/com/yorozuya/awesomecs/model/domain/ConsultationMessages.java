package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "consultation_messages")
@Data
public class ConsultationMessages implements Serializable {
    private Long id;
    private Long consultationId;
    private Long senderId;
    private String content;
    private String messageType;
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}
