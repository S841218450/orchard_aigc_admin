package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新AI作品DTO")
public class AiWorkUpdateDto {

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "结果URL")
    private String resultUrl;

    @Schema(description = "结果数据列表（多图场景）")
    private List<String> dataList;
}
