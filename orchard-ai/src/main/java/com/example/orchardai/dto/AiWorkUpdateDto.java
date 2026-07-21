package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新AI作品DTO")
public class AiWorkUpdateDto {

    @Schema(description = "结果URL")
    private String resultUrl;

    @Schema(description = "状态：0-待生成 1-生成中 2-已完成 3-失败")
    private Integer status;
}
