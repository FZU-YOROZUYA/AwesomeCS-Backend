package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.domain.Consultations;
import com.yorozuya.awesomecs.model.domain.ConsultationMessages;
import com.yorozuya.awesomecs.model.response.ConsultationMessageResponse;
import com.yorozuya.awesomecs.model.response.ConsultationResponse;
import com.yorozuya.awesomecs.service.ConsultationsService;
import com.yorozuya.awesomecs.service.ConsultationMessagesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationsController {

    @Resource
    private ConsultationsService consultationsService;

    @Resource
    private ConsultationMessagesService consultationMessagesService;

    @PostMapping("/book")
    @SaCheckLogin
    public Result<Object> book(@RequestParam("relation_id") Long relationId) {
        Long seekerId = StpUtil.getLoginIdAsLong();
        try {
            Long consultationId = consultationsService.bookConsultation(seekerId, relationId);
            return Result.buildSuccessResult(consultationId);
        } catch (RuntimeException e) {
            return Result.buildErrorResult(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay-callback")
    public Result<Object> payCallback(@PathVariable Long id, @RequestParam("transaction_id") String transactionId) {
        try {
            consultationsService.payCallback(id, transactionId);
            return Result.buildSuccessResult(null);
        } catch (RuntimeException e) {
            return Result.buildErrorResult(e.getMessage());
        }
    }

    @GetMapping("/me")
    @SaCheckLogin
    public Result<List<ConsultationResponse>> listMy() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Consultations> consultations = consultationsService.listMyConsultations(userId);
        List<ConsultationResponse> responses = consultations.stream()
                .map(this::convertToResponse)
                .toList();
        return Result.buildSuccessResult(responses);
    }

    @GetMapping("/{id}/messages")
    @SaCheckLogin
    public Result<List<ConsultationMessageResponse>> getMessages(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 验证用户是否参与此咨询
        Consultations consultation = consultationsService.getById(id);
        if (consultation == null
                || (!consultation.getSeekerId().equals(userId) && !consultation.getExpertId().equals(userId))) {
            return Result.buildErrorResult("Not authorized");
        }
        List<ConsultationMessages> messages = consultationMessagesService.lambdaQuery()
                .eq(ConsultationMessages::getConsultationId, id)
                .orderByAsc(ConsultationMessages::getCreatedAt)
                .list();
        List<ConsultationMessageResponse> responses = messages.stream()
                .map(this::convertMessageToResponse)
                .toList();
        return Result.buildSuccessResult(responses);
    }

    private ConsultationResponse convertToResponse(Consultations consultation) {
        ConsultationResponse response = new ConsultationResponse();
        response.setId(consultation.getId());
        response.setExpertId(consultation.getExpertId());
        response.setSeekerId(consultation.getSeekerId());
        response.setStatus(consultation.getStatus());
        response.setCreatedAt(consultation.getCreatedAt());
        return response;
    }

    private ConsultationMessageResponse convertMessageToResponse(ConsultationMessages message) {
        ConsultationMessageResponse response = new ConsultationMessageResponse();
        response.setId(message.getId());
        response.setConsultationId(message.getConsultationId());
        response.setSenderId(message.getSenderId());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
