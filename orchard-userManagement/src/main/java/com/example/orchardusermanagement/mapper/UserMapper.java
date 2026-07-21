package com.example.orchardusermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.orchardusermanagement.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
