package com.yorozuya.awesomecs.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yorozuya.awesomecs.model.domain.ConsultationPayments;
import com.yorozuya.awesomecs.model.domain.ConsultationRelation;
import com.yorozuya.awesomecs.model.domain.Consultations;
import com.yorozuya.awesomecs.repository.mapper.ConsultationPaymentsMapper;
import com.yorozuya.awesomecs.repository.mapper.ConsultationsMapper;
import com.yorozuya.awesomecs.service.ConsultationRelationService;
import com.yorozuya.awesomecs.service.ConsultationsService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author wjc28
 * @description 针对表【consultations(付费咨询表)】的数据库操作Service实现
 * @createDate 2025-11-01 15:55:02
 */
@Service
public class ConsultationsServiceImpl extends ServiceImpl<ConsultationsMapper, Consultations>
        implements ConsultationsService {

    @Resource
    private ConsultationsMapper consultationsMapper;

    @Resource
    private ConsultationRelationService consultationRelationService;

    @Resource
    private ConsultationPaymentsMapper consultationPaymentsMapper;

    @Override
    public Long bookConsultation(Long seekerId, Long relationId) {
        ConsultationRelation relation = consultationRelationService.getById(relationId);
        if (relation == null) {
            throw new RuntimeException("relation not found");
        }
        Consultations consultation = new Consultations();
        consultation.setId(IdUtil.getSnowflakeNextId());
        consultation.setSeekerId(seekerId);
        consultation.setExpertId(relation.getUserId());
        consultation.setStatus(0); // 待支付
        this.save(consultation);

        ConsultationPayments payment = new ConsultationPayments();
        payment.setConsultationId(consultation.getId());
        payment.setAmount(BigDecimal.valueOf(relation.getPrice()));
        payment.setStatus(0); // pending
        payment.setProvider("test"); // 模拟
        consultationPaymentsMapper.insert(payment);

        return consultation.getId();
    }

    @Override
    public void payCallback(Long consultationId, String transactionId) {
        Consultations consultation = this.getById(consultationId);
        if (consultation == null || consultation.getStatus() != 0) {
            throw new RuntimeException("invalid consultation");
        }
        consultation.setStatus(1); // 已预约
        this.updateById(consultation);

        // 更新支付记录
        LambdaQueryWrapper<ConsultationPayments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationPayments::getConsultationId, consultationId);
        ConsultationPayments payment = consultationPaymentsMapper.selectOne(wrapper);
        if (payment != null) {
            payment.setStatus(1); // success
            payment.setTransactionId(transactionId);
            consultationPaymentsMapper.updateById(payment);
        }
    }

    @Override
    public List<Consultations> listMyConsultations(Long userId) {
        LambdaQueryWrapper<Consultations> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultations::getSeekerId, userId).or().eq(Consultations::getExpertId, userId);
        return this.list(wrapper);
    }
}
