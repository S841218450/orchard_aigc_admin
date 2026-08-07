package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "消息VO（一段对话：提问+回答）")
public class ChatMessageVo {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "用户提问")
    private String question;

    @Schema(description = "AI回答")
    private String answer;

    @Schema(description = "提问附件列表")
    private List<AttachmentDto> attachments;

    @Schema(description = "状态：0-生成中 1-完成 2-失败")
    private Integer status;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "创建时间")
    private Long createTime;
}
