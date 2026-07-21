package com.example.orchardai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    List<ChatMessageVo> listBySessionId(Long sessionId);
}
