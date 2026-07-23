package com.example.orchardai.dto;

import com.example.orchardai.enums.WorkStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新AI作品DTO")
public class AiWorkUpdateDto {

    @Schema(description = "结果URL")
    private String resultUrl;

    @Schema(description = "状态")
    private WorkStatusEnum status;
}
