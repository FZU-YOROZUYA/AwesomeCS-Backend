package com.yorozuya.awesomecs.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 请求体：更新个人资料
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String bio;
    private List<String> techs;
    @JsonProperty("target_job")
    private String targetJob;
}
