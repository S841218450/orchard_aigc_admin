package com.example.orchardusermanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardusermanagement.entity.User;
import com.example.orchardusermanagement.mapper.UserMapper;
import com.example.orchardusermanagement.service.UserService;
import com.example.orchardusermanagement.dto.UserDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public void add(UserDto dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setId(SnowflakeUtils.nextId(BizCodeEnum.USER));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        save(user);
    }

    @Override
    public void update(Long id, UserDto dto) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        BeanUtils.copyProperties(dto, user);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }
}
