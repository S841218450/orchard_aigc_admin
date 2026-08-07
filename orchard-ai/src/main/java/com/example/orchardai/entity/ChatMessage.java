package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_message")
@Schema(description = "消息（一段对话=一条记录：提问+回答）")
public class ChatMessage extends baseEntity {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "用户提问")
    private String question;

    @Schema(description = "AI回答")
    private String answer;

    @Schema(description = "提问附件JSON")
    private String attachmentsJson;

    @Schema(description = "状态：0-生成中 1-完成 2-失败")
    private Integer status;

    @Schema(description = "失败原因")
    private String errorMsg;
}
