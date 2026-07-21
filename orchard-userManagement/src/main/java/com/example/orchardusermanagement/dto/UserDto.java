package com.example.orchardusermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户DTO")
public class UserDto {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "用户类型：1-公司 2-个人")
    private Integer userType;

    @Schema(description = "关联公司ID")
    private Long companyId;

    @Schema(description = "所属部门ID")
    private Long departmentId;

    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;
}
