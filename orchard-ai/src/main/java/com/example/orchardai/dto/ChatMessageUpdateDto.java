package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息更新 DTO（Agent 生成完回答后，回调 Java 更新消息的回答与状态）
 * <p>
 * 契约：PUT /api/ai/chat/update（@InternalApi，Agent 携带 X-Service-Key 调用）
 */
@Data
@Schema(description = "消息更新DTO")
public class ChatMessageUpdateDto {

    @NotNull(message = "消息ID不能为空")
    @Schema(description = "消息ID（一段对话的唯一ID）")
    private Long id;

    @Schema(description = "AI回答内容（status=1 时有值）")
    private String answer;

    @Schema(description = "状态：1-完成 2-失败")
    private Integer status;

    @Schema(description = "失败原因（status=2 时有值）")
    private String errorMsg;
}
