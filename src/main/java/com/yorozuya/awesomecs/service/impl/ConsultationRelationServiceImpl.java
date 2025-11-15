package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import com.yorozuya.awesomecs.model.domain.ConsultationRelation;
import com.yorozuya.awesomecs.model.request.CreateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.request.UpdateConsultationRelationRequest;
import com.yorozuya.awesomecs.repository.mapper.ConsultationRelationMapper;
import com.yorozuya.awesomecs.service.ConsultationRelationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ConsultationRelationServiceImpl extends ServiceImpl<ConsultationRelationMapper, ConsultationRelation>
        implements ConsultationRelationService {

    @Resource
    private ConsultationRelationMapper consultationRelationMapper;

    private static final Gson GSON = new Gson();

    @Override
    public Long createRelation(Long userId, CreateConsultationRelationRequest req) {
        // 检查用户是否已经创建过咨询关系
        LambdaQueryWrapper<ConsultationRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRelation::getUserId, userId);
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(Constants.ResponseCode.ONLY_ONE_CONSULTATION);
        }

        ConsultationRelation relation = new ConsultationRelation();
        relation.setUserId(userId);
        relation.setPrice(req.getPrice());
        relation.setDomains(req.getIntro());
        this.save(relation);
        return relation.getId();
    }

    @Override
    public void updateRelation(Long id, Long userId, UpdateConsultationRelationRequest req) {
        ConsultationRelation exist = this.getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new RuntimeException("not found or no permission");
        }
        exist.setPrice(req.getPrice());
        exist.setDomains(req.getIntro());
        this.updateById(exist);
    }

    @Override
    public List<ConsultationRelation> listByDomain(String domain) {
        LambdaQueryWrapper<ConsultationRelation> wrapper = new LambdaQueryWrapper<>();
        if (domain != null && !domain.isEmpty()) {
            wrapper.like(ConsultationRelation::getDomains, domain);
        }
        return this.list(wrapper);
    }
}
