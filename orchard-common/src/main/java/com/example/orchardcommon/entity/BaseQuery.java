package com.example.orchardcommon.entity;
import lombok.Data;

@Data
public class BaseQuery {
    private String query;

    private Integer pageNum;

    private Integer pageSize;

    // 常量限制
    private static final int MIN_PAGE_NUM = 1;
    private static final int MAX_PAGE_SIZE = 300;
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    // 获取页码，自动修正边界+默认值
    public Integer getPageNum() {
        if (pageNum == null || pageNum < MIN_PAGE_NUM) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    // 获取页大小，限制最大条数
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}