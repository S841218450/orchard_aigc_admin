package com.example.orchardai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardai.dto.ChatSessionDto;
import com.example.orchardai.dto.ChatSessionVo;
import com.example.orchardai.entity.ChatSession;

import java.util.List;

public interface ChatSessionService extends IService<ChatSession> {

    void add(Long sessionId, ChatSessionDto dto);

    void update(Long id, ChatSessionDto dto);

    List<ChatSessionVo> listByUserId(Long userId);
}
