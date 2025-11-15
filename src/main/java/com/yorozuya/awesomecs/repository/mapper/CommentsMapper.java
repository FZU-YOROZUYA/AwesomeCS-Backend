package com.yorozuya.awesomecs.repository.mapper;

import com.yorozuya.awesomecs.model.domain.Comments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wjc28
* @description 针对表【comments(文章评论表)】的数据库操作Mapper
* @createDate 2025-11-01 15:55:02
* @Entity generator.domain.Comments
*/
@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {

}




