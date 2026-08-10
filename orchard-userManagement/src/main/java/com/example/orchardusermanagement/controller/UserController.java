package com.example.orchardusermanagement.controller;

import com.example.orchardcommon.dto.IdDto;
import com.example.orchardcommon.result.Result;
import com.example.orchardusermanagement.dto.UserDto;
import com.example.orchardusermanagement.entity.User;
import com.example.orchardusermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserDto dto) {
        userService.add(dto);
        return Result.ok();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/updateUser")
    public Result<Void> update(@Valid @RequestBody UserDto dto,HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.update(userId, dto);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/deleted/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/getUserDetail")
    public Result<User> getById(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        return Result.ok(user);
    }
}
