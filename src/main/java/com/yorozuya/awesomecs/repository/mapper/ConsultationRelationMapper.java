package com.yorozuya.awesomecs.repository.mapper;

import com.yorozuya.awesomecs.model.domain.ConsultationRelation;
import com.yorozuya.awesomecs.model.response.ConsultationRelationResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ConsultationRelationMapper extends BaseMapper<ConsultationRelation> {

	List<ConsultationRelationResponse> selectRelationsPaged(@Param("domain") String domain,
													   @Param("excludeUserId") Long excludeUserId,
													   @Param("offset") Integer offset,
													   @Param("size") Integer size);

	Long selectRelationsPagedCount(@Param("domain") String domain,
									 @Param("excludeUserId") Long excludeUserId);

}
