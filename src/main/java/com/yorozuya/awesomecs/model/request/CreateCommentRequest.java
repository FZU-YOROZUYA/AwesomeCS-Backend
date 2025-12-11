package com.yorozuya.awesomecs.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @JsonProperty("parent_id")
    private String parentId; // 可为空，回复时传入父评论ID
    private String content;
}
