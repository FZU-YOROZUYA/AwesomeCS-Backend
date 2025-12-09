package com.yorozuya.awesomecs.service;

import com.yorozuya.awesomecs.model.domain.Consultations;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author wjc28
 * @description 针对表【consultations(付费咨询表)】的数据库操作Service
 * @createDate 2025-11-01 15:55:02
 */
public interface ConsultationsService extends IService<Consultations> {

    Long bookConsultation(Long seekerId, Long relationId);

    void payCallback(String consultationId, String transactionId);

    List<Consultations> listMyConsultations(Long userId);

    void endConsultation(Long consultationId, Long operatorUserId);
}
