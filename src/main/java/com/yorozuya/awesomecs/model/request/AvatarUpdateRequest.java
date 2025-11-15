package com.yorozuya.awesomecs.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求体：更新头像
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUpdateRequest {
    private String avatar;
}
