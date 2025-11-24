package com.yorozuya.awesomecs.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yorozuya.awesomecs.model.domain.PostLikes;
import com.yorozuya.awesomecs.repository.redis.RedisUtil;
import com.yorozuya.awesomecs.service.PostLikesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PostLikesTask {

    @Resource
    private PostLikesService postLikesService;

    @Resource
    private RedisUtil redisUtil;

    private static final String POST_LIKE_COUNT_KEY = "post:likes:count:";

    // 每小时执行一次
    @Scheduled(cron = "0 0 * * * ?")
    public void syncPostLikesToRedis() {
        log.info("开始同步文章点赞数到 Redis...");
        try {
            // 查询所有文章的点赞数
            // SELECT post_id as postId, count(*) as count FROM post_likes GROUP BY post_id
            QueryWrapper<PostLikes> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("post_id as postId", "count(*) as count");
            queryWrapper.groupBy("post_id");
            
            List<Map<String, Object>> list = postLikesService.listMaps(queryWrapper);
            
            if (list != null && !list.isEmpty()) {
                for (Map<String, Object> map : list) {
                    // 尝试获取 postId，兼容不同的大小写情况
                    Object postIdObj = map.get("postId");
                    if (postIdObj == null) {
                        postIdObj = map.get("post_id");
                    }
                    if (postIdObj == null) {
                        continue;
                    }
                    
                    Long postId = Long.valueOf(postIdObj.toString());
                    
                    // 注意：count(*) 返回的类型可能是 Long 或 Integer，取决于数据库驱动
                    Number countNum = (Number) map.get("count");
                    int count = countNum.intValue();
                    
                    String key = POST_LIKE_COUNT_KEY + postId;
                    redisUtil.set(key, count);
                }
            }
            log.info("同步文章点赞数完成，共同步 {} 条记录", list == null ? 0 : list.size());
        } catch (Exception e) {
            log.error("同步文章点赞数失败", e);
        }
    }
}
