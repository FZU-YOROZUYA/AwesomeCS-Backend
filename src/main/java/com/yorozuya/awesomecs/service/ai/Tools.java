package com.yorozuya.awesomecs.service.ai;


import com.yorozuya.awesomecs.model.domain.Posts;
import com.yorozuya.awesomecs.model.domain.Users;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.model.response.PostSummaryResponse;
import com.yorozuya.awesomecs.repository.mapper.UsersMapper;
import com.yorozuya.awesomecs.service.PostLikesService;
import com.yorozuya.awesomecs.service.PostsService;
import com.yorozuya.awesomecs.service.UsersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class Tools {
    @Resource
    private UsersService usersService;
    @Resource
    private PostsService postService;

    @Tool(description = "根据用户的 uid 获取用户的个人介绍。用户的个人介绍里包括了用户的目标岗位和掌握的技术张")
    String getUserBioByUid(@ToolParam(description = "userId") Long uid){
        Users user = usersService.getById(uid);
        log.info("{}'s bio was selected", uid);
        return user.getBio();
    }

    @Tool(description = "根据用户的 uid 获取用户最近点赞的 10 篇博客。")
    List<Posts> getTenLikesByUid(@ToolParam(description = "userId") Long uid){
        log.info("{}'s ten likes was selected", uid);
        PageResponse<PostSummaryResponse> rst = postService.listUserLikedPosts(uid, 0, 10);
        return rst.getList().stream().map(postSummaryResponse -> {
            return postService.getById(postSummaryResponse.getId());
        }).toList();
    }
}
