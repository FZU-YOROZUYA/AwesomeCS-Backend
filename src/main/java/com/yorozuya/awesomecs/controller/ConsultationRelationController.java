package com.yorozuya.awesomecs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.Result;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import com.yorozuya.awesomecs.model.domain.ConsultationRelation;
import com.yorozuya.awesomecs.model.request.CreateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.request.UpdateConsultationRelationRequest;
import com.yorozuya.awesomecs.model.response.ConsultationRelationResponse;
import com.yorozuya.awesomecs.model.response.PageResponse;
import com.yorozuya.awesomecs.service.ConsultationRelationService;
import com.yorozuya.awesomecs.service.UsersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/consultation-relations")
@CrossOrigin
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

    @GetMapping("/{consultation_relations_id}")
    @SaCheckLogin
    public Result<ConsultationRelationResponse> getInfo(@PathVariable("consultation_relations_id") String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<ConsultationRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRelation::getUserId, userId)
                .eq(ConsultationRelation::getId, id);
        ConsultationRelation consultationRelation = consultationRelationService.getOne(wrapper);
        if (consultationRelation == null) {
            throw new BusinessException(Constants.ResponseCode.NO_OBJECT);
        }
        return Result.buildSuccessResult(
                new ConsultationRelationResponse(
                        null,
                        null,
                        "",
                        consultationRelation.getPrice(),
                        consultationRelation.getDomains(),
                        null,
                        null,
                        null
                )
        );
    }

    @GetMapping("/been")
    @SaCheckLogin
    public Result<String> getBeen() {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<ConsultationRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRelation::getUserId, userId);
        ConsultationRelation one = consultationRelationService.getOne(wrapper);
        if  (one == null) {
            return Result.buildSuccessResult("-1");
        }
        return  Result.buildSuccessResult(one.getId().toString());
    }

    @GetMapping
    public Result<PageResponse<ConsultationRelationResponse>> listResult(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "domain", required = false) String domain) {
        Long excludeUserId = null;
        if (StpUtil.isLogin()) {
            excludeUserId = StpUtil.getLoginIdAsLong();
        }
        PageResponse<ConsultationRelationResponse> resp = consultationRelationService.listRelationsPaged(page, size, domain, excludeUserId);
        return Result.buildSuccessResult(resp);
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

}
