package com.example.orchardusermanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_plan")
@Schema(description = "套餐")
public class Plan extends baseEntity {

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "套餐描述")
    private String description;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "有效期（天）")
    private Integer durationDays;

    @Schema(description = "用户类型：1-公司 2-个人 0-通用")
    private Integer userType;

    @Schema(description = "状态：0-下架 1-上架")
    private Integer status;
}
