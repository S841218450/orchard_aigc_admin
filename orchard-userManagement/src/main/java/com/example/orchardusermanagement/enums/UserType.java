package com.example.orchardusermanagement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserType {
    COMPANY(1, "公司"),
    INDIVIDUAL(2, "个人");

    private final Integer code;
    private final String desc;
}
