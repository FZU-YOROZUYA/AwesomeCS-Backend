package com.yorozuya.awesomecs.repository.mapper;

import com.yorozuya.awesomecs.model.domain.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wjc28
* @description 针对表【users(用户基础信息表)】的数据库操作Mapper
* @createDate 2025-11-01 15:55:02
* @Entity generator.domain.Users
*/
@Mapper
public interface UsersMapper extends BaseMapper<Users> {

}




