package com.example.orchardcommon.business.SnowflakeId;

import lombok.Getter;

public enum BizCodeEnum {
    //AI模块
    SESSION(2581, "会话ID", "SESS"),
    MESSAGE(2593, "消息", "MSG"),
    WORK(2605, "作品", "WORK"),

    //文件系统
    FILE(1080, "文件", "FILE"),

    //业务
    USER(5000, "用户", "USER"),
    COMPANY(5001, "公司", "COMP"),
    DEPARTMENT(5002, "部门", "DEPT"),
    PLAN(5003, "计划", "PLAN"),
    USER_SUBSCRIPTION(5004, "用户订阅", "USUB"),

    //系统相关
    SYSTEM(1040, "系统", "SYS"),
    EXTEND1(1050, "扩展1", "EXT1"),
    EXTEND2(1060, "扩展2", "EXT2"),
    EXTEND3(1070, "扩展3", "EXT3");


    @Getter
    private final int code;    // 雪花内部业务码（你原来的，完全不动）
    @Getter
    private final String name;
    @Getter
    private final String prefix; // 给人看的编号前缀（新增）

    // 构造方法也同步改好
    BizCodeEnum(int code, String name, String prefix) {
        this.code = code;
        this.name = name;
        this.prefix = prefix;
    }
}
