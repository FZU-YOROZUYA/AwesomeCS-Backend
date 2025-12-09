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
        int offset = (cur - 1) * sz;
        List<PostSummaryResponse> list = postsMapper.selectPostSummaries(
                (keyword == null ? null : keyword.trim()),
                (category == null ? null : category.trim()),
                (tag == null ? null : tag.trim()),
                offset,
                sz
        );
        Long total = postsMapper.selectPostSummariesCount(
                (keyword == null ? null : keyword.trim()),
                (category == null ? null : category.trim()),
                (tag == null ? null : tag.trim())
        );
        return new PageResponse<>(list == null ? new ArrayList<>() : list, total == null ? 0L : total);
    }

    @Override
    public String getPostsCount(String category) {
        Long rst = postsMapper.selectPostSummariesCount(
                null,
                (category == null ? null : category.trim()),
                null
        );
        return Long.toString(rst);
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
            item.setId(String.valueOf(p.get("id")));
            item.setTitle(ps.getTitle());
            item.setSummary(ps.getSummary());
            item.setCategory(ps.getCategory());
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
        // 使用连表查询获取作者昵称与头像（从 mapper 返回的 map 中读取）
        try {
            Map<String, Object> m = postsMapper.selectPostDetailMap(id);
            if (m != null) {
                Object an = m.get("author_name");
                if (an != null) resp.setAuthorName(String.valueOf(an));
                Object aa = m.get("author_avatar");
                if (aa != null) resp.setAuthorAvatar(String.valueOf(aa));
            }
        } catch (Exception ignore) {
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
        int offset = (cur - 1) * sz;

        List<PostSummaryResponse> list = postsMapper.selectUserLikedPostSummaries(userId, offset, sz);
        Long total = postsMapper.selectUserLikedPostSummariesCount(userId);

        return new PageResponse<>(list == null ? new ArrayList<>() : list, total == null ? 0L : total);
    }

    @Override
    public PageResponse<PostSummaryResponse> listPostsByUser(Long userId, Integer page, Integer size, String keyword, String category, String tag) {
        int cur = (page == null || page <= 0) ? 1 : page;
        int sz = (size == null || size <= 0) ? 20 : size;
        int offset = (cur - 1) * sz;
        List<PostSummaryResponse> list = postsMapper.selectPostSummariesByUser(
                userId,
                (keyword == null ? null : keyword.trim()),
                (category == null ? null : category.trim()),
                (tag == null ? null : tag.trim()),
                offset,
                sz
        );
        Long total = postsMapper.selectPostSummariesByUserCount(
                userId,
                (keyword == null ? null : keyword.trim()),
                (category == null ? null : category.trim()),
                (tag == null ? null : tag.trim())
        );
        return new PageResponse<>(list == null ? new ArrayList<>() : list, total == null ? 0L : total);
    }

    @Override
    public Integer getTotalLikesByUser(Long userId) {
        if (userId == null) return 0;
        // 查询该用户的所有文章 id
        List<Posts> posts = postsMapper.selectList(new LambdaQueryWrapper<Posts>().select(Posts::getId).eq(Posts::getUserId, userId).eq(Posts::getStatus, 1));
        if (posts == null || posts.isEmpty()) return 0;
        List<Long> ids = new ArrayList<>();
        for (Posts p : posts) ids.add(p.getId());
        int cnt = Math.toIntExact(postLikesMapper.selectCount(new LambdaQueryWrapper<PostLikes>().in(PostLikes::getPostId, ids)));
        return cnt;
    }

    @Override
    public Long getTotalViewsByUser(Long userId) {
        if (userId == null) return 0L;
        // 使用聚合查询 sum(view_count)
        QueryWrapper<Posts> qw = new QueryWrapper<>();
        qw.select("COALESCE(SUM(view_count),0) AS total_views");
        qw.eq("user_id", userId).eq("status", 1);
        List<Map<String, Object>> res = postsMapper.selectMaps(qw);
        if (res == null || res.isEmpty()) return 0L;
        Object v = res.get(0).get("total_views");
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception ex) {
            return 0L;
        }
    }

    @Override
    public Long getTotalPostsByUser(Long userId) {
        if (userId == null) return 0L;
        Long cnt = postsMapper.selectCount(new QueryWrapper<Posts>().eq("user_id", userId).eq("status", 1));
        return cnt == null ? 0L : cnt;
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
