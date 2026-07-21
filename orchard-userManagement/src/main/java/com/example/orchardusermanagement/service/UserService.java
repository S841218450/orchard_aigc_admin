package com.example.orchardusermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardusermanagement.entity.User;
import com.example.orchardusermanagement.dto.UserDto;

public interface UserService extends IService<User> {

    void add(UserDto dto);

    void update(Long id, UserDto dto);
}
