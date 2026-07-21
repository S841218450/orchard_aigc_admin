package com.example.orchardcommon.business.SnowflakeId;

import org.springframework.beans.factory.annotation.Value;

/**
 * 双模式雪花ID工具类
 * 1. nextId()     ：乱序 ID（适合用户、供应商、商品、基础数据）
 * 2. nextSeqId()  ：带自增序列号 ID（适合订单、采购单、出库单、单据）
 */
public class SnowflakeUtils {
//    生成格式 业务码+时间戳+随机数

    // 起始时间 2026-01-01
    private static final long EPOCH = 1767225600000L;

    // 机器ID（单机=0，集群0~63）
    @Value("${snowflake.worker.id:0}")
    private static long workerId;

    // 随机模式（乱序，无自增）
    public static synchronized long nextId(BizCodeEnum biz) {
        return nextIdInternal(biz.getCode(), false);
    }

    // 单据模式（末尾自增序列号）
    public static synchronized long nextSeqId(BizCodeEnum biz) {
        return nextIdInternal(biz.getCode(), true);
    }

    // ==================== 内部实现 ====================
    private static long lastTimestamp = -1;
    private static int sequence = 0;
    private static final int MAX_SEQ = 4095;

    private static long nextIdInternal(int bizCode, boolean needSequence) {
        long now = System.currentTimeMillis();

        // 时间回拨保护
        if (now < lastTimestamp) {
            throw new RuntimeException("时间回拨，禁止生成ID");
        }

        // 单据模式：自增序列号
        if (needSequence) {
            if (now == lastTimestamp) {
                sequence = (sequence + 1) & MAX_SEQ;
                if (sequence == 0) now = nextMillis(lastTimestamp);
            } else {
                sequence = 0;
            }
        }
        // 乱序模式：不使用自增序列号

        lastTimestamp = now;

        // 生成 19位 雪花ID
        return ((now - EPOCH) << 22)
                | ((bizCode & 0xFFFFL) << 6)
                | (workerId & 0x3F);
    }

    private static long nextMillis(long last) {
        long now = System.currentTimeMillis();
        while (now <= last) now = System.currentTimeMillis();
        return now;
    }
}
