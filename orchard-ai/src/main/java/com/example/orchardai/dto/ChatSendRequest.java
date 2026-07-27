package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发送消息请求")
public class ChatSendRequest {

    @Schema(description = "会话ID，为空则新建会话")
    private Long sessionId;

    @Schema(description = "消息内容")
    private String message;

    @Schema(description = "附件列表")
    private List<AttachmentDto> attachments;
}