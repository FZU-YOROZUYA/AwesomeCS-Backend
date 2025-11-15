package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.model.domain.PostLikes;
import com.yorozuya.awesomecs.repository.mapper.PostLikesMapper;
import com.yorozuya.awesomecs.service.PostLikesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wjc28
 * @description 针对表【post_likes(文章点赞表)】的数据库操作Service实现
 * @createDate 2025-11-01 15:55:02
 */
@Service
public class PostLikesServiceImpl extends ServiceImpl<PostLikesMapper, PostLikes>
        implements PostLikesService {

    @Resource
    private PostLikesMapper postLikesMapper;

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        // 先查当前用户是否已点赞
        LambdaQueryWrapper<PostLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLikes::getUserId, userId).eq(PostLikes::getPostId, postId);
        PostLikes one = postLikesMapper.selectOne(wrapper);

        Map<String, Object> rst = new HashMap<>();
        if (one != null) {
            // 已点赞 -> 取消（只操作 post_likes 表）
            postLikesMapper.deleteById(one.getId());
            int likeCount = Math.toIntExact(
                    postLikesMapper.selectCount(new LambdaQueryWrapper<PostLikes>().eq(PostLikes::getPostId, postId)));
            rst.put("action", "cancelled");
            rst.put("newLikeCount", likeCount);
            return rst;
        } else {
            PostLikes entity = new PostLikes();
            entity.setUserId(userId);
            entity.setPostId(postId);
            postLikesMapper.insert(entity);
            int likeCount = Math.toIntExact(
                    postLikesMapper.selectCount(new LambdaQueryWrapper<PostLikes>().eq(PostLikes::getPostId, postId)));
            rst.put("action", "liked");
            rst.put("newLikeCount", likeCount);
            return rst;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null)
            return false;
        LambdaQueryWrapper<PostLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLikes::getUserId, userId).eq(PostLikes::getPostId, postId);
        return getOne(wrapper) != null;
    }

    @Override
    public int countLikes(Long postId) {
        LambdaQueryWrapper<PostLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLikes::getPostId, postId);
        return Math.toIntExact(count(wrapper));
    }

}
