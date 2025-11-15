package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.model.domain.MockInterviews;
import com.yorozuya.awesomecs.repository.mapper.MockInterviewsMapper;
import com.yorozuya.awesomecs.service.MockInterviewsService;
import org.springframework.stereotype.Service;

/**
* @author wjc28
* @description 针对表【mock_interviews(模拟面试记录表)】的数据库操作Service实现
* @createDate 2025-11-01 15:55:02
*/
@Service
public class MockInterviewsServiceImpl extends ServiceImpl<MockInterviewsMapper, MockInterviews>
    implements MockInterviewsService {

}




