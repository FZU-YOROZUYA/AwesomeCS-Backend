package com.yorozuya.awesomecs.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRelationResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private String username;

    private Double price;

    private String info;


    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("consultation_count")
    private Integer consultationCount;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createdAt;
}
