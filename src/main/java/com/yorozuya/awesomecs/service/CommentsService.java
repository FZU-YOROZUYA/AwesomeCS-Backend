package com.yorozuya.awesomecs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yorozuya.awesomecs.model.domain.Comments;
import com.yorozuya.awesomecs.model.response.CommentResponse;

public interface CommentsService extends IService<Comments> {
    // 新增：创建评论（含回复）
    Comments createComment(Long postId, Long userId, Long parentId, String content);

    // 分页获取某文章的评论，返回带回复的树形结构（只分页顶级评论）
    Page<CommentResponse> listCommentsByPost(Long postId,
                                             int page, int size);
}
