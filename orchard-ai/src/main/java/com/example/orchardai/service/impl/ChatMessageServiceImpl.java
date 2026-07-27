package com.example.orchardai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardai.dto.AttachmentDto;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.entity.ChatMessage;
import com.example.orchardai.mapper.ChatMessageMapper;
import com.example.orchardai.service.ChatMessageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private final ObjectMapper objectMapper;

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
        
        // 解析附件JSON
        if (StringUtils.hasText(msg.getAttachmentsJson())) {
            try {
                List<AttachmentDto> attachments = objectMapper.readValue(
                    msg.getAttachmentsJson(), 
                    new TypeReference<List<AttachmentDto>>() {}
                );
                vo.setAttachments(attachments);
            } catch (Exception e) {
                log.error("解析附件JSON失败, messageId={}", msg.getId(), e);
            }
        }
        
        return vo;
    }
}
