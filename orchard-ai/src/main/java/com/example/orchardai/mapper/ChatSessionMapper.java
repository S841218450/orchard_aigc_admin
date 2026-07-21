package com.example.orchardai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orchardai.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
