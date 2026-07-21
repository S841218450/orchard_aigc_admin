package com.example.orchardai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardai.dto.ChatSessionDto;
import com.example.orchardai.dto.ChatSessionVo;
import com.example.orchardai.entity.ChatSession;
import com.example.orchardai.mapper.ChatSessionMapper;
import com.example.orchardai.service.ChatSessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Override
    public void add(Long sessionId, ChatSessionDto dto) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long userId = (Long) attributes.getRequest().getAttribute("userId");
        ChatSession session = new ChatSession();
        BeanUtils.copyProperties(dto, session);
        session.setId(sessionId);
        session.setUserId(userId);
        session.setStatus(1);
        session.setCreateTime(LocalDateTime.now());
        save(session);
    }

    @Override
    public void update(Long id, ChatSessionDto dto) {
        ChatSession session = getById(id);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        session.setTitle(dto.getTitle());
        session.setUpdateTime(LocalDateTime.now());
        updateById(session);
    }

    @Override
    public List<ChatSessionVo> listByUserId(Long userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
               .eq(ChatSession::getStatus, 1)
               .orderByDesc(ChatSession::getCreateTime);
        return list(wrapper).stream().map(this::toVo).collect(Collectors.toList());
    }

    private ChatSessionVo toVo(ChatSession session) {
        ChatSessionVo vo = new ChatSessionVo();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setCreateTime(session.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return vo;
    }
}
