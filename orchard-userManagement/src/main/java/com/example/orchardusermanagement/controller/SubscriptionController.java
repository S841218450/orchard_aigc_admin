package com.example.orchardusermanagement.controller;

import com.example.orchardcommon.result.Result;
import com.example.orchardusermanagement.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public Result<Void> subscribe(@RequestParam Long userId, @RequestParam Long planId) {
        subscriptionService.subscribe(userId, planId);
        return Result.ok();
    }

    @Operation(summary = "取消订阅")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        subscriptionService.cancel(id);
        return Result.ok();
    }
}
