package com.yorozuya.awesomecs.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponse {
    @JsonProperty("consultation_id")
    private Long consultationId;

    @JsonProperty("expert_id")
    private Long expertId;

    @JsonProperty("seeker_id")
    private Long seekerId;

    private Integer status;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createdAt;
    
    @JsonProperty("expert_name")
    private String expertName;

    @JsonProperty("expert_avatar")
    private String expertAvatar;

    @JsonProperty("seeker_name")
    private String seekerName;

    @JsonProperty("seeker_avatar")
    private String seekerAvatar;

    @JsonProperty("is_expert")
    private Boolean isExpert;
}
