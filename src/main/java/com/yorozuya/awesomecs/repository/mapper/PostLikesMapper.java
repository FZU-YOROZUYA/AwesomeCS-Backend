package com.yorozuya.awesomecs.repository.mapper;

import com.yorozuya.awesomecs.model.domain.PostLikes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author wjc28
* @description 针对表【post_likes(文章点赞表)】的数据库操作Mapper
* @createDate 2025-11-01 15:55:02
* @Entity generator.domain.PostLikes
*/
@Mapper
public interface PostLikesMapper extends BaseMapper<PostLikes> {
}




