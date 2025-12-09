package com.yorozuya.awesomecs.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationMessageResponse {
    @JsonProperty("msg_id")
    private Long msgId;

    @JsonProperty("consultation_id")
    private Long consultationId;

    @JsonProperty("sender_id")
    private Long senderId;

    private String content;

    @JsonProperty("message_type")
    private String messageType;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}
