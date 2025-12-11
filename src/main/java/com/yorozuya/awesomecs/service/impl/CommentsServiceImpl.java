package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import com.yorozuya.awesomecs.model.domain.Posts;
import com.yorozuya.awesomecs.repository.mapper.CommentsMapper;
import com.yorozuya.awesomecs.model.domain.Comments;
import com.yorozuya.awesomecs.model.domain.Users;
import com.yorozuya.awesomecs.model.response.CommentResponse;
import com.yorozuya.awesomecs.repository.mapper.PostsMapper;
import com.yorozuya.awesomecs.repository.mapper.UsersMapper;
import com.yorozuya.awesomecs.service.CommentsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentsServiceImpl extends ServiceImpl<com.yorozuya.awesomecs.repository.mapper.CommentsMapper, Comments>
        implements CommentsService {

    @Resource
    private PostsMapper postsMapper;

    @Resource
    private CommentsMapper commentsMapper;

    @Resource
    private UsersMapper usersMapper;

    @Override
    public Comments createComment(String postId, Long userId, String parentId, String content) {
        Posts posts = postsMapper.selectById(postId);
        if (posts == null) {
            throw new BusinessException(Constants.ResponseCode.NO_OBJECT);
        }
        if (parentId == null) {
            Comments pc = commentsMapper.selectById(parentId);
            if (pc == null) {
                throw new BusinessException(Constants.ResponseCode.NO_OBJECT);
            }
        }



        Comments comment = new Comments();
        comment.setPostId(Long.parseLong(postId));
        comment.setUserId(userId);
        comment.setParentId(Long.parseLong(parentId) );
        comment.setContent(content);
        comment.setCreatedAt(new java.util.Date());
        this.save(comment);
        return comment;
    }

    @Override
    public Page<CommentResponse> listCommentsByPost(Long postId, int page, int size) {
        // 分页查询顶级评论
        Page<Comments> topPage = new Page<>(page, size);
        LambdaQueryWrapper<Comments> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.eq(Comments::getPostId, postId).isNull(Comments::getParentId).orderByDesc(Comments::getCreatedAt);
        Page<Comments> topCommentsPage = this.page(topPage, topWrapper);

        List<Long> topIds = topCommentsPage.getRecords().stream().map(Comments::getId).collect(Collectors.toList());

        List<Comments> replies = new ArrayList<>();
        if (!topIds.isEmpty()) {
            LambdaQueryWrapper<Comments> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comments::getPostId, postId).in(Comments::getParentId, topIds)
                    .orderByAsc(Comments::getCreatedAt);
            replies = this.list(replyWrapper);
        }

        // 将用户信息批量查询
        List<Long> userIds = new ArrayList<>();
        userIds.addAll(topCommentsPage.getRecords().stream().map(Comments::getUserId).collect(Collectors.toList()));
        userIds.addAll(replies.stream().map(Comments::getUserId).collect(Collectors.toList()));
        List<Users> users = new ArrayList<>();
        if (!userIds.isEmpty()) {
            users = usersMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()));
        }
        Map<Long, Users> userMap = users.stream().collect(Collectors.toMap(Users::getId, u -> u));

        // 组织回复映射 parentId -> List<Comments>
        Map<Long, List<Comments>> repliesMap = replies.stream().collect(Collectors.groupingBy(Comments::getParentId));

        List<CommentResponse> responses = topCommentsPage.getRecords().stream().map(c -> {
            CommentResponse cr = new CommentResponse();
            cr.setId(c.getId().toString());
            cr.setPostId(c.getPostId().toString());
            cr.setUserId(c.getUserId().toString());
            cr.setContent(c.getContent());
            cr.setCreatedAt(c.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            Users u = userMap.get(c.getUserId());
            if (u != null) {
                cr.setUserName(u.getNickname());
                cr.setAvatar(u.getAvatar());
            }
            List<Comments> rs = repliesMap.getOrDefault(c.getId(), new ArrayList<>());
            List<CommentResponse> childResponses = rs.stream().map(rc -> {
                CommentResponse crc = new CommentResponse();
                crc.setId(rc.getId().toString());
                crc.setPostId(rc.getPostId().toString());
                crc.setUserId(rc.getUserId().toString());
                crc.setContent(rc.getContent());
                crc.setCreatedAt(
                        rc.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                Users ru = userMap.get(rc.getUserId());
                if (ru != null) {
                    crc.setUserName(ru.getNickname());
                    crc.setAvatar(ru.getAvatar());
                }
                return crc;
            }).collect(Collectors.toList());
            cr.setReplies(childResponses);
            return cr;
        }).collect(Collectors.toList());

        Page<CommentResponse> result = new Page<>(topCommentsPage.getCurrent(), topCommentsPage.getSize(),
                topCommentsPage.getTotal());
        result.setRecords(responses);
        return result;
    }
}
