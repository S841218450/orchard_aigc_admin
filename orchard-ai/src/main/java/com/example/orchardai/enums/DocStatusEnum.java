package com.example.orchardai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "向量化中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败");

    private final int code;
    private final String label;
}
