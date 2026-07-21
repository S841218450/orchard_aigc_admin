package com.example.orchardusermanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_subscription")
@Schema(description = "用户订阅")
public class UserSubscription extends baseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "套餐ID")
    private Long planId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订阅状态：1-生效中 2-已过期 3-已取消 4-待生效")
    private Integer status;

    @Schema(description = "订阅价格")
    private BigDecimal price;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;
}
