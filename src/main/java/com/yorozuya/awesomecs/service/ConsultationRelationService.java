package com.yorozuya.awesomecs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yorozuya.awesomecs.model.domain.ConsultationRelation;
import com.yorozuya.awesomecs.model.request.CreateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.request.UpdateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.response.ConsultationRelationResponse;
import com.yorozuya.awesomecs.model.response.PageResponse;

import java.util.List;

public interface ConsultationRelationService extends IService<ConsultationRelation> {

    Long createRelation(Long userId, CreateConsultationRelationRequest req);

    void updateRelation(Long id, Long userId, UpdateConsultationRelationRequest req);

    PageResponse<ConsultationRelationResponse> listRelationsPaged(Integer page, Integer size, String domain, Long excludeUserId);
}
