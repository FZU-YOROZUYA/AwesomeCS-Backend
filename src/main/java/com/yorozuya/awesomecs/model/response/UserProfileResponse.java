package com.yorozuya.awesomecs.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    @JsonProperty("target_job")
    private String targetJob;
    private List<String> techs;
}
