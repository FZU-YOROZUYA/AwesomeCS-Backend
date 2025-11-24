package com.yorozuya.awesomecs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yorozuya.awesomecs.model.domain.MockInterviews;
import com.yorozuya.awesomecs.model.request.CreateInterviewRequest;

/**
 * Interview 领域服务接口（用于创建面试等业务操作）
 */
public interface InterviewService extends IService<MockInterviews> {

    /**
     * 创建一个面试记录并返回其 ID
     * @param req 包含 domain 和 style
     * @param userId 发起/所属用户 id
     * @return 新建面试记录的 id
     */
    Long createInterview(CreateInterviewRequest req, Long userId);

}
