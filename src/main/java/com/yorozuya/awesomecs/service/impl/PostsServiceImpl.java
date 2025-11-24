package com.yorozuya.awesomecs.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yorozuya.awesomecs.model.domain.PostLikes;
import com.yorozuya.awesomecs.model.domain.Posts;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.model.response.PostDetailResponse;
import com.yorozuya.awesomecs.model.response.PostSummaryResponse;
import com.yorozuya.awesomecs.repository.mapper.PostLikesMapper;
import com.yorozuya.awesomecs.repository.mapper.PostsMapper;
import com.yorozuya.awesomecs.service.PostsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wjc28
 * @description 针对表【posts(博客文章表)】的数据库操作Service实现
 * @createDate 2025-11-01 15:55:02
 */
@Service
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts>
        implements PostsService {

    @Resource
    private PostsMapper postsMapper;

    @Resource
    private PostLikesMapper postLikesMapper;

    private static final Gson GSON = new Gson();

    @Override
    public PageResponse<PostSummaryResponse> listPosts(Integer page, Integer size, String keyword, String category,
            String tag) {
        int cur = (page == null || page <= 0) ? 1 : page;
        int sz = (size == null || size <= 0) ? 20 : size;
        Page<Posts> pg = new Page<>(cur, sz);
        LambdaQueryWrapper<Posts> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Posts::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            String k = keyword.trim();
            // keyword 同时搜索标题、内容、分类和 tags 字段（简单的字符串匹配）
            wrapper.and(w -> w.like(Posts::getTitle, k)
                    .or().like(Posts::getContent, k)
                    .or().like(Posts::getCategory, k)
                    .or().like(Posts::getTags, k));
        }
        // category 精确匹配
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Posts::getCategory, category);
        }
        // tag 查询：利用 JSON_CONTAINS(tags, '"tag"')，MySQL 专有写法，使用 wrapper.apply
        if (tag != null && !tag.isEmpty()) {
            String normalized = tag.trim().toLowerCase();
            wrapper.apply("JSON_CONTAINS(tags, '\"" + normalized + "\"')");
        }
        IPage<Posts> res = postsMapper.selectPage(pg, wrapper);
        List<PostSummaryResponse> list = new ArrayList<>();
        for (Posts p : res.getRecords()) {
            PostSummaryResponse item = new PostSummaryResponse();
            item.setId(p.getId());
            item.setTitle(p.getTitle());
            item.setSummary(p.getSummary());
            item.setAuthor(String.valueOf(p.getUserId()));
            item.setCreateTime(p.getCreatedAt());
            item.setViewCount(p.getViewCount());
            list.add(item);
        }
        return new PageResponse<>(list, res.getTotal());
    }

    /**
     * 用点赞量和查阅量来进行排序，优先点赞量，其次查阅量
     * */
    @Override
    public PageResponse<PostSummaryResponse> listPopularPosts(Integer page, Integer size) {
        int cur = (page == null || page <= 0) ? 1 : page;
        int sz = (size == null || size <= 0) ? 20 : size;
        Page<Map<String,Object>> pg = new Page<>(cur, sz);
        QueryWrapper<PostLikes> wrapper = new QueryWrapper<>();
        wrapper.select("post_likes.post_id as p_id",
                        "count(*) as p_count",
                        "posts.view_count as p_view_count ")
                .apply("left join posts on posts.id=post_likes.post_id")
                .eq("posts.status", 1)
                .groupBy("post_likes.post_id", "posts.view_count")
                .orderByDesc("p_count")
                .orderByDesc("p_view_count");
        IPage<Map<String,Object>> res = postLikesMapper.selectMapsPage(pg,wrapper);//获得当前页的热门博客的id
        List<PostSummaryResponse> list = new ArrayList<>();
        for(Map<String,Object> p : res.getRecords()){
            if(p.get("p_id")==null) continue;
            Posts ps=postsMapper.selectOne(new QueryWrapper<Posts>()
                                            .eq("id",p.get("p_id")));
            if(ps==null) continue;
            PostSummaryResponse item = new PostSummaryResponse();
            item.setId(ps.getId());
            item.setTitle(ps.getTitle());
            item.setSummary(ps.getSummary());
            item.setAuthor(String.valueOf(ps.getUserId()));
            item.setCreateTime(ps.getCreatedAt());
            item.setViewCount(ps.getViewCount());
            list.add(item);
        }
        return new PageResponse<>(list, res.getTotal());
    }

    @Override
    public PostDetailResponse getPostDetail(Long id, Long currentUserId) {
        Posts p = postsMapper.selectById(id);
        if (p == null || p.getStatus() == 2) {
            return null;
        }
        // 每次查看文章详情时，阅读数 +1
        Integer newView = (p.getViewCount() == null) ? 1 : p.getViewCount() + 1;
        p.setViewCount(newView);
        postsMapper.updateById(p);

        PostDetailResponse resp = new PostDetailResponse();
        resp.setId(p.getId());
        resp.setTitle(p.getTitle());
        resp.setContent(p.getContent());
        resp.setAuthor(String.valueOf(p.getUserId()));
        resp.setCreateTime(p.getCreatedAt());
        // tags: 把 JSON tags 转为 List
        if (p.getTags() != null && !p.getTags().isEmpty()) {
            try {
                List<String> tags = GSON.fromJson(p.getTags(), new TypeToken<List<String>>() {
                }.getType());
                resp.setTags(tags != null ? tags : new ArrayList<>());
            } catch (Exception ex) {
                resp.setTags(new ArrayList<>());
            }
        } else {
            resp.setTags(new ArrayList<>());
        }
        // like count: 统计 post_likes 表中的记录数
        int likeCount = Math.toIntExact(
                postLikesMapper.selectCount(new LambdaQueryWrapper<PostLikes>().eq(PostLikes::getPostId, id)));
        resp.setLikeCount(likeCount);
        if (currentUserId != null) {
            resp.setIsLiked(postLikesMapper
                    .selectCount(new LambdaQueryWrapper<PostLikes>()
                            .eq(PostLikes::getUserId, currentUserId).eq(PostLikes::getPostId, id)) > 0);
        } else {
            resp.setIsLiked(false);
        }
        return resp;
    }

    @Override
    public Long createPost(com.yorozuya.awesomecs.model.request.CreatePostRequest req, Long userId) {
        Posts p = new Posts();
        long id = IdUtil.getSnowflakeNextId();
        p.setId(id);
        p.setUserId(userId);
        p.setCategory(req.getCategory());
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());
        // 如果前端没有提供 summary，则自动生成摘要（取内容前 150 字）
        if (req.getSummary() == null || req.getSummary().isEmpty()) {
            String summary = generateSummary(req.getContent(), 150);
            p.setSummary(summary);
        } else {
            p.setSummary(req.getSummary());
        }
        p.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        p.setViewCount(0);
        // tags: 将 List<String> 转为 JSON 字符串存储
        if (req.getTags() != null) {
            p.setTags(GSON.toJson(req.getTags()));
        }
        postsMapper.insert(p);
        return id;
    }

    @Override
    public void updatePost(Long id, com.yorozuya.awesomecs.model.request.UpdatePostRequest req, Long userId) {
        Posts p = postsMapper.selectById(id);
        if (p == null)
            return;
        if (!p.getUserId().equals(userId))
            throw new RuntimeException("无权限");
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());
        p.setSummary(req.getSummary());
        p.setCategory(req.getCategory());
        p.setStatus(req.getStatus());
        // tags update
        if (req.getTags() != null) {
            p.setTags(GSON.toJson(req.getTags()));
        }
        postsMapper.updateById(p);
    }

    @Override
    public void deletePost(Long id, Long userId) {
        Posts p = postsMapper.selectById(id);
        if (p == null)
            return;
        if (!p.getUserId().equals(userId))
            throw new RuntimeException("无权限");
        p.setStatus(2);
        postsMapper.updateById(p);
    }

    @Override
    public PageResponse<PostSummaryResponse> listUserLikedPosts(Long userId, Integer page, Integer size) {
        int cur = (page == null || page <= 0) ? 1 : page;
        int sz = (size == null || size <= 0) ? 20 : size;
        Page<PostLikes> pg = new Page<>(cur, sz);
        LambdaQueryWrapper<PostLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLikes::getUserId, userId);
        IPage<PostLikes> res = postLikesMapper.selectPage(pg, wrapper);
        List<Long> postIds = new ArrayList<>();
        for (PostLikes pl : res.getRecords())
            postIds.add(pl.getPostId());
        if (postIds.isEmpty())
            return new PageResponse<>(new ArrayList<>(), 0);
        // 查询 posts
        Page<Posts> postPage = new Page<>(cur, sz);
        LambdaQueryWrapper<Posts> pw = new LambdaQueryWrapper<>();
        pw.in(Posts::getId, postIds).eq(Posts::getStatus, 1);
        IPage<Posts> postsRes = postsMapper.selectPage(postPage, pw);
        List<PostSummaryResponse> list = new ArrayList<>();
        for (Posts p2 : postsRes.getRecords()) {
            PostSummaryResponse item = new PostSummaryResponse();
            item.setId(p2.getId());
            item.setTitle(p2.getTitle());
            item.setSummary(p2.getSummary());
            item.setAuthor(String.valueOf(p2.getUserId()));
            item.setCreateTime(p2.getCreatedAt());
            item.setViewCount(p2.getViewCount());
            list.add(item);
        }
        return new PageResponse<>(list, postsRes.getTotal());
    }

    private static String generateSummary(String content, int maxChars) {
        if (content == null)
            return "";
        String noHtml = content.replaceAll("(?s)<[^>]*>", "");
        String txt = noHtml.replaceAll("\\s+", " ").trim();
        if (txt.length() <= maxChars)
            return txt;
        int endIdx = Math.min(txt.length(), maxChars);
        // 尽量在空格处分割
        String cut = txt.substring(0, endIdx);
        int lastSpace = cut.lastIndexOf(' ');
        if (lastSpace > maxChars * 0.6) {
            cut = cut.substring(0, lastSpace);
        }
        return cut.trim() + "...";
    }

}
