package com.example.orchardai.dto;

import com.example.orchardcommon.entity.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会话消息分页查询参数")
public class ChatMessageQuery extends BaseQuery {

    @Schema(description = "会话ID")
    private Long sessionId;
}
