package com.yorozuya.awesomecs.repository.mapper;

import com.yorozuya.awesomecs.model.domain.Posts;
import com.yorozuya.awesomecs.model.response.PostSummaryResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
* @author wjc28
* @description 针对表【posts(博客文章表)】的数据库操作Mapper
* @createDate 2025-11-01 15:55:02
* @Entity generator.domain.Posts
*/
@Mapper
public interface PostsMapper extends BaseMapper<Posts> {
	// 自定义连表查询，返回文章摘要（包含作者、头像、点赞数、评论数）
	List<PostSummaryResponse> selectPostSummaries(@Param("keyword") String keyword,
												  @Param("category") String category,
												  @Param("tag") String tag,
												  @Param("offset") Integer offset,
												  @Param("size") Integer size);

	Long selectPostSummariesCount(@Param("keyword") String keyword,
								  @Param("category") String category,
								  @Param("tag") String tag);

	// 按作者列出文章摘要（用于我的博客）
	List<PostSummaryResponse> selectPostSummariesByUser(@Param("userId") Long userId,
								  @Param("keyword") String keyword,
								  @Param("category") String category,
								  @Param("tag") String tag,
								  @Param("offset") Integer offset,
								  @Param("size") Integer size);

	Long selectPostSummariesByUserCount(@Param("userId") Long userId,
							  @Param("keyword") String keyword,
							  @Param("category") String category,
							  @Param("tag") String tag);

	// 查询用户点赞过的文章列表（分页）
	List<PostSummaryResponse> selectUserLikedPostSummaries(@Param("userId") Long userId,
							   @Param("offset") Integer offset,
							   @Param("size") Integer size);

	Long selectUserLikedPostSummariesCount(@Param("userId") Long userId);

	/**
	 * 连表查询文章详情并返回列-值映射（包含作者昵称与头像）
	 */
	Map<String, Object> selectPostDetailMap(@Param("id") Long id);
}




