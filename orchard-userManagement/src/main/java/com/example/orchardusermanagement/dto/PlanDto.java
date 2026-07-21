package com.example.orchardusermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "套餐DTO")
public class PlanDto {

    @NotBlank(message = "套餐名称不能为空")
    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "套餐描述")
    private String description;

    @NotNull(message = "价格不能为空")
    @Schema(description = "价格")
    private BigDecimal price;

    @NotNull(message = "有效期不能为空")
    @Schema(description = "有效期（天）")
    private Integer durationDays;

    @Schema(description = "用户类型：1-公司 2-个人 0-通用")
    private Integer userType;
}
