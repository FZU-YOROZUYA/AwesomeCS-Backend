package com.yorozuya.awesomecs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import com.yorozuya.awesomecs.model.domain.ConsultationRelation;
import com.yorozuya.awesomecs.model.request.CreateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.request.UpdateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.response.ConsultationRelationResponse;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.repository.mapper.ConsultationRelationMapper;
import com.yorozuya.awesomecs.service.ConsultationRelationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultationRelationServiceImpl extends ServiceImpl<ConsultationRelationMapper, ConsultationRelation>
        implements ConsultationRelationService {

    @Resource
    private ConsultationRelationMapper consultationRelationMapper;

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
        relation.setDomains(req.getInfo() == null ? "" : req.getInfo());
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
        exist.setDomains(req.getInfo() == null ? "" : req.getInfo());
        this.updateById(exist);
    }


    @Override
        public PageResponse<ConsultationRelationResponse> listRelationsPaged(Integer page, Integer size, String domain, Long excludeUserId) {
        int cur = (page == null || page <= 0) ? 1 : page;
        int sz = (size == null || size <= 0) ? 20 : size;
        int offset = (cur - 1) * sz;

        List<ConsultationRelationResponse> list = consultationRelationMapper.selectRelationsPaged(
                (domain == null ? null : domain.trim()),
            excludeUserId,
                offset,
                sz
        );
        Long total = consultationRelationMapper.selectRelationsPagedCount(
            (domain == null ? null : domain.trim()),
            excludeUserId
        );

        return new PageResponse<>(list == null ? new ArrayList<>() : list, total == null ? 0L : total);
    }
}
