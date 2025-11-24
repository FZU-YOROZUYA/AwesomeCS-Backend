package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.model.domain.ConsultationRelation;
import com.yorozuya.awesomecs.model.domain.Users;
import com.yorozuya.awesomecs.model.request.CreateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.request.UpdateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.response.ConsultationRelationResponse;
import com.yorozuya.awesomecs.service.ConsultationRelationService;
import com.yorozuya.awesomecs.service.UsersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/consultation-relations")
public class ConsultationRelationController {

    @Resource
    private ConsultationRelationService consultationRelationService;

    @Resource
    private UsersService usersService;

    @PostMapping
    @SaCheckLogin
    public Result<Object> create(@RequestBody CreateConsultationRelationRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = consultationRelationService.createRelation(userId, req);
        return Result.buildSuccessResult(id);
    }

    @GetMapping
    public Result<List<ConsultationRelationResponse>> list(@RequestParam(value = "domain", required = false) String domain) {
        List<ConsultationRelation> relations = consultationRelationService.listByDomain(domain);
        List<ConsultationRelationResponse> responses = relations.stream()
                .map(this::convertToResponse)
                .toList();
        return Result.buildSuccessResult(responses);
    }

    @PutMapping("/{id}")
    @SaCheckLogin
    public Result<Object> update(@PathVariable Long id, @RequestBody UpdateConsultationRelationRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            consultationRelationService.updateRelation(id, userId, req);
            return Result.buildSuccessResult(null);
        } catch (RuntimeException e) {
            return Result.buildErrorResult(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @SaCheckLogin
    public Result<Object> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        ConsultationRelation exist = consultationRelationService.getById(id);
        if (exist == null)
            return Result.buildErrorResult("not found");
        if (!exist.getUserId().equals(userId))
            return Result.buildErrorResult("no permission");
        consultationRelationService.removeById(id);
        return Result.buildSuccessResult(null);
    }

    private ConsultationRelationResponse convertToResponse(ConsultationRelation relation) {
        ConsultationRelationResponse response = new ConsultationRelationResponse();
        response.setId(relation.getId());
        response.setUserId(relation.getUserId());
        response.setPrice(BigDecimal.valueOf(relation.getPrice()));
        // 解析 domains JSON 字符串为 List<String>
        if (relation.getDomains() != null && !relation.getDomains().isEmpty()) {
            Gson gson = new Gson();
            List<String> domainsList = gson.fromJson(relation.getDomains(), new TypeToken<List<String>>(){}.getType());
            response.setDomains(domainsList);
        }
        response.setCreatedAt(relation.getCreatedAt());

        // 查询用户头像
        Users user = usersService.getById(relation.getUserId());
        if (user != null) {
            response.setAvatarUrl(user.getAvatar());
        }

        return response;
    }
}
