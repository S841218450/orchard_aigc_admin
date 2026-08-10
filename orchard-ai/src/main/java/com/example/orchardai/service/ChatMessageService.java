package com.example.orchardai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardai.dto.ChatMessageQuery;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.entity.ChatMessage;
import com.example.orchardcommon.result.PageResult;

public interface ChatMessageService extends IService<ChatMessage> {

    PageResult<ChatMessageVo> pageBySessionId(ChatMessageQuery query);
}
