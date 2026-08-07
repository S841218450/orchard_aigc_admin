package com.example.orchardusermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "取消订阅请求")
public class CancelSubscribeDto {

    @NotNull(message = "订阅记录ID不能为空")
    @Schema(description = "订阅记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
