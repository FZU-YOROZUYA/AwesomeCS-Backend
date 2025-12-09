package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.model.domain.MockInterviews;
import com.yorozuya.awesomecs.model.request.CreateInterviewRequest;
import com.yorozuya.awesomecs.repository.mapper.MockInterviewsMapper;
import com.yorozuya.awesomecs.service.InterviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Interview 服务实现，封装创建面试的业务逻辑
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<MockInterviewsMapper, MockInterviews>
        implements InterviewService {

    @Resource
    private MockInterviewsMapper mockInterviewsMapper;

    @Override
    public Long createInterview(CreateInterviewRequest req, Long userId) {
        MockInterviews record = new MockInterviews();
        record.setUserId(userId);
        record.setDomain(req.getDomain());
        record.setStyle(req.getStyle());
        record.setCreatedAt(new Date());
        this.save(record);
        return record.getId();
    }

}
