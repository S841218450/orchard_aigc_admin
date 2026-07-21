package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_message")
@Schema(description = "消息")
public class ChatMessage extends baseEntity {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "角色：user-用户 assistant-AI")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "排序")
    private Integer sort;
}
