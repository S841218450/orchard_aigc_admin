package com.example.orchardusermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "订阅套餐请求")
public class SubscribeDto {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "套餐ID不能为空")
    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long planId;
}
