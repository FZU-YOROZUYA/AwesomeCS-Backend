package com.yorozuya.awesomecs.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "consultation_payments")
@Data
public class ConsultationPayments implements Serializable {
    private Long id;
    private Long consultationId;
    private java.math.BigDecimal amount;
    private Integer status;
    private String provider;
    private String transactionId;
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}
