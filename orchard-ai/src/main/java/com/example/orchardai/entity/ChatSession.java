package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_chat_session")
@Schema(description = "会话")
public class ChatSession extends baseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "状态：0-已删除 1-正常")
    private Integer status;
}
