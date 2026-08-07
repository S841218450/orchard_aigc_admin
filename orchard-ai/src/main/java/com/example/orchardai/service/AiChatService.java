package com.example.orchardai.service;

import com.example.orchardai.dto.ChatMessageUpdateDto;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.dto.ChatSendRequest;

public interface AiChatService {

    ChatMessageVo send(ChatSendRequest request);

    void delete(Long messageId);

    /**
     * Agent 生成完回答回调：更新消息的回答与状态
     */
    void update(ChatMessageUpdateDto dto);
}
