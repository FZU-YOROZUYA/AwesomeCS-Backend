package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.domain.Consultations;
import com.yorozuya.awesomecs.model.domain.ConsultationMessages;
import com.yorozuya.awesomecs.model.domain.Users;
import com.yorozuya.awesomecs.model.response.ConsultationMessageResponse;
import com.yorozuya.awesomecs.model.response.ConsultationResponse;
import com.yorozuya.awesomecs.repository.mapper.UsersMapper;
import com.yorozuya.awesomecs.service.ConsultationsService;
import com.yorozuya.awesomecs.service.ConsultationMessagesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/consultations")
@CrossOrigin
public class ConsultationsController {

    @Resource
    private ConsultationsService consultationsService;

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private ConsultationMessagesService consultationMessagesService;

    @PostMapping("/book")
    @SaCheckLogin
    public Result<String> book(@RequestParam("relation_id") String relationId) {
        Long seekerId = StpUtil.getLoginIdAsLong();
        Long consultationId = consultationsService.bookConsultation(seekerId, Long.parseLong(relationId));
        return Result.buildSuccessResult(consultationId.toString());
    }

    @PostMapping("/{id}/pay-callback")
    @SaCheckLogin
    public Result<String> payCallback(@PathVariable String id, @RequestParam("transaction_id") String transactionId) {
        consultationsService.payCallback(id, transactionId);
        return Result.buildSuccessResult(null);
    }

    @GetMapping("/me")
    @SaCheckLogin
    public Result<List<ConsultationResponse>> listMy() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Consultations> consultations = consultationsService.listMyConsultations(userId);
        // collect all participant ids (expert + seeker)
        Set<Long> userIds = new HashSet<>();
        for (Consultations c : consultations) {
            if (c.getExpertId() != null) userIds.add(c.getExpertId());
            if (c.getSeekerId() != null) userIds.add(c.getSeekerId());
        }
        Map<Long, Users> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Users> users = usersMapper.selectByIds(userIds);
            for (Users u : users) {
                userMap.put(u.getId(), u);
            }
        }

        List<ConsultationResponse> responses = consultations.stream().map(c -> {
            ConsultationResponse r = convertToResponse(c);
            if (Objects.equals(r.getExpertId(), userId)) {
                r.setIsExpert(Boolean.TRUE);
            }else {
                r.setIsExpert(Boolean.FALSE);
            }
            Users expert = userMap.get(c.getExpertId());
            if (expert != null) {
                r.setExpertName(expert.getNickname());
                r.setExpertAvatar(expert.getAvatar());
            }
            Users seeker = userMap.get(c.getSeekerId());
            if (seeker != null) {
                r.setSeekerName(seeker.getNickname());
                r.setSeekerAvatar(seeker.getAvatar());
            }
            return r;
        }).toList();

        return Result.buildSuccessResult(responses);
    }

    @GetMapping("/{id}/messages")
    @SaCheckLogin
    public Result<List<ConsultationMessageResponse>> getMessages(@PathVariable String id) {
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

    @PutMapping("/{id}/end")
    @SaCheckLogin
    public Result<String> endConsultation(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        consultationsService.endConsultation(id, userId);
        return Result.buildSuccessResult("consultation ended");
    }

    private ConsultationResponse convertToResponse(Consultations consultation) {
        ConsultationResponse response = new ConsultationResponse();
        response.setConsultationId(consultation.getId());
        response.setExpertId(consultation.getExpertId());
        response.setSeekerId(consultation.getSeekerId());
        response.setStatus(consultation.getStatus());
        response.setCreatedAt(consultation.getCreatedAt());
        return response;
    }

    private ConsultationMessageResponse convertMessageToResponse(ConsultationMessages message) {
        ConsultationMessageResponse response = new ConsultationMessageResponse();
        response.setMsgId(message.getId());
        response.setConsultationId(message.getConsultationId());
        response.setSenderId(message.getSenderId());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
