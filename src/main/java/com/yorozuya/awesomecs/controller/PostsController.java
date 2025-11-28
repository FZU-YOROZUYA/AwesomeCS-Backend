package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.request.CreatePostRequest;
import com.yorozuya.awesomecs.model.request.UpdatePostRequest;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.model.response.PostDetailResponse;
import com.yorozuya.awesomecs.model.response.PostSummaryResponse;
import com.yorozuya.awesomecs.service.PostLikesService;
import com.yorozuya.awesomecs.service.PostsService;
import com.yorozuya.awesomecs.model.request.CreateCommentRequest;
import com.yorozuya.awesomecs.model.response.CommentResponse;
import com.yorozuya.awesomecs.service.CommentsService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostsController {

    @Autowired
    @Qualifier("PostsService")
    private PostsService postsService;

    @Resource
    private PostLikesService postLikesService;

    @Resource
    private CommentsService commentsService;
//获取博客列表
    @GetMapping
    @SaIgnore
    public Result<PageResponse<PostSummaryResponse>> listPosts(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value="category",required=false) String category,
            @RequestParam(value="tag",required=false) String tag
            ) {
        PageResponse<PostSummaryResponse> resp = postsService.listPosts(page, size, keyword, category, tag);
        return Result.buildSuccessResult(resp);
    }
//获取热门博客列表
    @GetMapping("/popular")
    @SaIgnore
    public Result<PageResponse<PostSummaryResponse>> listPopularPosts(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
            ) {
        PageResponse<PostSummaryResponse> resp = postsService.listPopularPosts(page, size);
        return Result.buildSuccessResult(resp);
    }
//获取博客详情
    @GetMapping("/{id}")
    @SaIgnore
    public Result<PostDetailResponse> getPost(@PathVariable("id") Long id) {
        Long currentUserId = null;
        if (StpUtil.isLogin()) {
            currentUserId = StpUtil.getLoginIdAsLong();
        }
        PostDetailResponse resp = postsService.getPostDetail(id, currentUserId);
        return Result.buildSuccessResult(resp);
    }

//    创建博客
    @PostMapping
    @SaCheckLogin
    public Result<Object> createPost(@RequestBody CreatePostRequest req, @RequestHeader("Authorization") String token) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = postsService.createPost(req, userId);
        return Result.buildSuccessResult(id);
    }
//    修改博客
    @PutMapping("/{id}")
    @SaCheckLogin
    public Result<Object> updatePost(@PathVariable Long id, @RequestBody UpdatePostRequest req, @RequestHeader("Authorization") String token) {
        Long userId = StpUtil.getLoginIdAsLong();
        postsService.updatePost(id, req, userId);
        return Result.buildSuccessResult(null);
    }
//     删除博客
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public Result<Object> deletePost(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = StpUtil.getLoginIdAsLong();
        postsService.deletePost(id, userId);
        return Result.buildSuccessResult(null);
    }

    @PostMapping("/{id}/like")
    @SaCheckLogin
    public Result<Object> toggleLike(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = StpUtil.getLoginIdAsLong();
        var resp = postLikesService.toggleLike(userId, id);
        return Result.buildSuccessResult(resp);
    }

    @GetMapping("/interaction/{id}")
    @SaCheckLogin
    public Result<Object> interaction(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = null;
        if (StpUtil.isLogin())
            userId = StpUtil.getLoginIdAsLong();
        int likeCount = postLikesService.countLikes(id);
        boolean isLiked = userId != null && postLikesService.isLiked(userId, id);
        Map<String, Object> rst = new HashMap<>();
        rst.put("likeCount", likeCount);
        rst.put("isLiked", isLiked);
        return Result.buildSuccessResult(rst);
    }

    @GetMapping("/me/liked")
    @SaCheckLogin
    public Result<PageResponse<PostSummaryResponse>> myLiked(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestHeader("Authorization") String token) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageResponse<PostSummaryResponse> resp = postsService.listUserLikedPosts(userId, page, size);
        return Result.buildSuccessResult(resp);
    }

    @PostMapping("/{postId}/comments")
    @SaCheckLogin
    public Result<Object> createComment(@PathVariable String postId, @RequestBody CreateCommentRequest req, @RequestHeader("Authorization") String token) {
        Long userId = StpUtil.getLoginIdAsLong();
        // parentId 可以通过 body 传递
        long pid = Long.parseLong(postId);
        Long createdId = commentsService.createComment(pid, userId, req.getParentId(), req.getContent()).getId();
        return Result.buildSuccessResult(createdId);
    }

    @GetMapping("/{postId}/comments")
    @SaIgnore
    public Result<PageResponse<CommentResponse>> listComments(@PathVariable Long postId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        int p = (page == null || page <= 0) ? 1 : page;
        int s = (size == null || size <= 0) ? 20 : size;
        var resp = commentsService.listCommentsByPost(postId, p, s);
        Page<CommentResponse> pageResp = resp;
        // 转换为 PageResponse
        PageResponse<CommentResponse> pr = new PageResponse<>((List<CommentResponse>) pageResp.getRecords(),
                pageResp.getTotal());
        return Result.buildSuccessResult(pr);
    }
}
