package com.example.orchardai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orchardai.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
