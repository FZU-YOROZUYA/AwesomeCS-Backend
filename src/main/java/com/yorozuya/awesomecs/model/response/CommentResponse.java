package com.yorozuya.awesomecs.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    @JsonProperty("comment_id")
    private String id;

    @JsonProperty("post_id")
    private String postId;

    @JsonProperty("user_id")
    private String userId;

    private String content;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @JsonProperty("user_name")
    private String userName;

    private String avatar;

    private List<CommentResponse> replies;
}
