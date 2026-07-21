package com.example.orchardusermanagement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionStatus {
    ACTIVE(1, "生效中"),
    EXPIRED(2, "已过期"),
    CANCELLED(3, "已取消"),
    PENDING(4, "待生效");

    private final Integer code;
    private final String desc;
}
