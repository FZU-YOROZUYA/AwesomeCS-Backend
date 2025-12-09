package com.yorozuya.awesomecs.model.response;

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
    private String targetJob;
    private List<String> techs;
}
