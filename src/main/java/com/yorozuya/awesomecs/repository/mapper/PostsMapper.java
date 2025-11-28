package com.yorozuya.awesomecs.repository.mapper;

import com.yorozuya.awesomecs.model.domain.Posts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
* @author wjc28
* @description 针对表【posts(博客文章表)】的数据库操作Mapper
* @createDate 2025-11-01 15:55:02
* @Entity generator.domain.Posts
*/
@Mapper
public interface PostsMapper extends BaseMapper<Posts> {

}




