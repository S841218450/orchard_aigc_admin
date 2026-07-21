package com.example.orchardusermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardusermanagement.entity.UserSubscription;

public interface UserSubscriptionService extends IService<UserSubscription> {

    void subscribe(Long userId, Long planId);

    void cancel(Long id);
}
