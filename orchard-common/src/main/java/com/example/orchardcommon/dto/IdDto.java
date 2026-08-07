package com.example.orchardcommon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "通用ID请求")
public class IdDto {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID")
    private Long id;
}
