package com.example.orchardai.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.orchardai.dto.AttachmentDto;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.dto.ChatSessionDto;
import com.example.orchardai.dto.ChatSendRequest;
import com.example.orchardai.entity.ChatMessage;
import com.example.orchardai.service.AiChatService;
import com.example.orchardai.service.ChatMessageService;
import com.example.orchardai.service.ChatSessionService;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    @Value("${ai.python.url:http://localhost:8000}")
    private String pythonUrl;

    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public ChatMessageVo send(ChatSendRequest request) {
        Long sessionId = request.getSessionId();
        String userMessage = request.getMessage();
        
        // 没有会话ID就新建会话
        if (sessionId == null) {
            sessionId = SnowflakeUtils.nextId(BizCodeEnum.SESSION);
            ChatSessionDto dto = new ChatSessionDto();
            dto.setTitle(userMessage.length() > 20 ? userMessage.substring(0, 20) + "..." : userMessage);
            chatSessionService.add(sessionId, dto);
        }
        // 保存用户消息并返回
        return saveMessage(sessionId, "user", userMessage, request.getAttachments());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long messageId) {
        ChatMessage message = chatMessageService.getById(messageId);
        if (message == null) {
            return;
        }
        
        Long sessionId = message.getSessionId();
        
        // 删除消息
        chatMessageService.removeById(messageId);
        
        // 判断是否是会话的最后一条消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        long count = chatMessageService.count(wrapper);
        
        // 如果没有消息了，删除会话
        if (count == 0) {
            chatSessionService.removeById(sessionId);
        }
    }

    //    保存消息
    private ChatMessageVo saveMessage(Long sessionId, String role, String content, List<AttachmentDto> attachments) {
        ChatMessage msg = new ChatMessage();
        msg.setId(SnowflakeUtils.nextId(BizCodeEnum.MESSAGE));
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        
        // 保存附件信息
        if (attachments != null && !attachments.isEmpty()) {
            try {
                msg.setAttachmentsJson(objectMapper.writeValueAsString(attachments));
            } catch (JsonProcessingException e) {
                log.error("序列化附件信息失败", e);
            }
        }
        
        chatMessageService.save(msg);

        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(msg.getId());
        vo.setSessionId(sessionId);
        vo.setRole(role);
        vo.setContent(content);
        vo.setCreateTime(LocalDateTimeUtil.toEpochMilli(msg.getCreateTime()));
        
        // 设置附件信息到返回对象
        if (attachments != null && !attachments.isEmpty()) {
            vo.setAttachments(attachments);
        }
        
        return vo;
    }

}
