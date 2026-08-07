package com.example.orchardusermanagement.controller;

import com.example.orchardcommon.result.Result;
import com.example.orchardusermanagement.dto.CancelSubscribeDto;
import com.example.orchardusermanagement.dto.SubscribeDto;
import com.example.orchardusermanagement.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订阅管理")
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private UserSubscriptionService subscriptionService;

    @Operation(summary = "订阅套餐")
    @PostMapping("/subscribe")
    public Result<Void> subscribe(@Valid @RequestBody SubscribeDto dto) {
        subscriptionService.subscribe(dto.getUserId(), dto.getPlanId());
        return Result.ok();
    }

    @Operation(summary = "取消订阅")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody CancelSubscribeDto dto) {
        subscriptionService.cancel(dto.getId());
        return Result.ok();
    }
}
