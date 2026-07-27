package com.example.orchardai.service;

import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.dto.ChatSendRequest;

public interface AiChatService {

    ChatMessageVo send(ChatSendRequest request);

    void delete(Long messageId);
}
