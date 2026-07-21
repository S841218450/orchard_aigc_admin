package com.example.orchardai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkStatusEnum {

    WAITING(0, "等待中"),
    GENERATING(1, "生成中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败"),
    PENDING_OPERATION(4, "待操作");

    private final int code;
    private final String label;
}