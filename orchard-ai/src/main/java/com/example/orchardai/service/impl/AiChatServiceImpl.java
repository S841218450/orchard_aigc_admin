package com.example.orchardai.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.orchardai.dto.AttachmentDto;
import com.example.orchardai.dto.ChatMessageUpdateDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;

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

        // 落库一段对话（提问 + 待生成的回答），状态为生成中
        ChatMessage msg = new ChatMessage();
        msg.setId(SnowflakeUtils.nextId(BizCodeEnum.MESSAGE));
        msg.setSessionId(sessionId);
        msg.setQuestion(userMessage);
        msg.setStatus(0);
        msg.setCreateTime(LocalDateTime.now());
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            try {
                msg.setAttachmentsJson(objectMapper.writeValueAsString(request.getAttachments()));
            } catch (JsonProcessingException e) {
                log.error("序列化附件信息失败", e);
            }
        }
        chatMessageService.save(msg);
        log.info("消息已落库：id={}, sessionId={}, 待 Agent 生成回答", msg.getId(), sessionId);

        return toVo(msg, request.getAttachments());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long messageId) {
        ChatMessage message = chatMessageService.getById(messageId);
        if (message == null) {
            return;
        }

        Long sessionId = message.getSessionId();

        // 删除整段消息（一段对话=一条记录）
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

    @Override
    public void update(ChatMessageUpdateDto dto) {
        ChatMessage msg = chatMessageService.getById(dto.getId());
        if (msg == null) {
            log.warn("AI回答回调，消息不存在：id={}", dto.getId());
            return;
        }
        msg.setAnswer(dto.getAnswer());
        msg.setStatus(dto.getStatus());
        msg.setErrorMsg(dto.getErrorMsg());
        chatMessageService.updateById(msg);
        log.info("Agent 回调更新消息：id={}, status={}", dto.getId(), dto.getStatus());
    }

    private ChatMessageVo toVo(ChatMessage msg, List<AttachmentDto> attachments) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(msg.getId());
        vo.setSessionId(msg.getSessionId());
        vo.setQuestion(msg.getQuestion());
        vo.setAnswer(msg.getAnswer());
        vo.setStatus(msg.getStatus());
        vo.setErrorMsg(msg.getErrorMsg());
        vo.setCreateTime(LocalDateTimeUtil.toEpochMilli(msg.getCreateTime()));
        if (attachments != null && !attachments.isEmpty()) {
            vo.setAttachments(attachments);
        }
        return vo;
    }
}
