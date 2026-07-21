package com.example.orchardai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.entity.ChatMessage;
import com.example.orchardai.mapper.ChatMessageMapper;
import com.example.orchardai.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Override
    public List<ChatMessageVo> listBySessionId(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getSort);
        return list(wrapper).stream().map(this::toVo).collect(Collectors.toList());
    }

    private ChatMessageVo toVo(ChatMessage msg) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(msg.getId());
        vo.setSessionId(msg.getSessionId());
        vo.setRole(msg.getRole());
        vo.setContent(msg.getContent());
        vo.setCreateTime(msg.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return vo;
    }
}
