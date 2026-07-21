package com.example.orchardusermanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardusermanagement.entity.Plan;
import com.example.orchardusermanagement.entity.UserSubscription;
import com.example.orchardusermanagement.enums.SubscriptionStatus;
import com.example.orchardusermanagement.mapper.UserSubscriptionMapper;
import com.example.orchardusermanagement.service.PlanService;
import com.example.orchardusermanagement.service.UserSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserSubscriptionServiceImpl extends ServiceImpl<UserSubscriptionMapper, UserSubscription> implements UserSubscriptionService {

    @Autowired
    private PlanService planService;

    @Override
    @Transactional
    public void subscribe(Long userId, Long planId) {
        Plan plan = planService.getById(planId);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        UserSubscription subscription = new UserSubscription();
        subscription.setId(SnowflakeUtils.nextId(BizCodeEnum.USER_SUBSCRIPTION));
        subscription.setUserId(userId);
        subscription.setPlanId(planId);
        subscription.setOrderNo(generateOrderNo());
        subscription.setStatus(SubscriptionStatus.ACTIVE.getCode());
        subscription.setPrice(plan.getPrice());
        subscription.setStartTime(LocalDateTime.now());
        subscription.setEndTime(LocalDateTime.now().plusDays(plan.getDurationDays()));
        subscription.setPayTime(LocalDateTime.now());
        subscription.setCreateTime(LocalDateTime.now());
        save(subscription);
    }

    @Override
    public void cancel(Long id) {
        UserSubscription subscription = getById(id);
        if (subscription == null) {
            throw new RuntimeException("订阅不存在");
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED.getCode());
        subscription.setUpdateTime(LocalDateTime.now());
        updateById(subscription);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
