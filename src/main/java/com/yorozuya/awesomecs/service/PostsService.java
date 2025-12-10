package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.model.domain.Posts;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yorozuya.awesomecs.model.request.CreatePostRequest;
import com.yorozuya.awesomecs.model.request.UpdatePostRequest;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.model.response.PostDetailResponse;
import com.yorozuya.awesomecs.model.response.PostSummaryResponse;

/**
 * @author wjc28
 * @description 针对表【posts(博客文章表)】的数据库操作Service
 * @createDate 2025-11-01 15:55:02
 */
public interface PostsService extends IService<Posts> {

    // 支持 keyword, category, tag 三种可选过滤
    PageResponse<PostSummaryResponse> listPosts(Integer page, Integer size, String keyword, String category,
            String tag);

    String getPostsCount(String category);

    PageResponse<PostSummaryResponse> listPopularPosts(Integer page, Integer siz);

    PostDetailResponse getPostDetail(Long id, Long currentUserId);

    Long createPost(CreatePostRequest req, Long userId);

    void updatePost(Long id, UpdatePostRequest req, Long userId);

    void deletePost(Long id, Long userId);

    // 列出用户点赞过的文章（分页）
    PageResponse<PostSummaryResponse> listUserLikedPosts(Long userId, Integer page, Integer size);

        // 列出当前用户本人发布的文章（分页、支持筛选，与 listPosts 返回类型一致）
        PageResponse<PostSummaryResponse> listPostsByUser(Long userId, Integer page, Integer size, String keyword, String category,
            String tag);

    /**
     * 获取用户发布的所有文章的总点赞数
     */
    Integer getTotalLikesByUser(Long userId);

    /**
     * 获取用户发布的所有文章的总浏览量
     */
    Long getTotalViewsByUser(Long userId);

    /**
     * 获取用户发布的文章总数
     */
    Long getTotalPostsByUser(Long userId);

}
