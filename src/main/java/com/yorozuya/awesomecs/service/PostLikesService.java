package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.model.domain.PostLikes;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yorozuya.awesomecs.model.response.PostDetailResponse;
import com.yorozuya.awesomecs.model.response.PageResponse;

import java.util.Map;

/**
 * @author wjc28
 * @description 针对表【post_likes(文章点赞表)】的数据库操作Service
 * @createDate 2025-11-01 15:55:02
 */
public interface PostLikesService extends IService<PostLikes> {

    /**
     * 切换点赞状态，返回 action("liked"/"cancelled") 与当前 likeCount
     */
    Map<String, Object> toggleLike(Long userId, Long postId);

    boolean isLiked(Long userId, Long postId);

    int countLikes(Long postId);

}
