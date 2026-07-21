package com.example.orchardusermanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
@Schema(description = "用户")
public class User extends baseEntity {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "用户类型：1-公司 2-个人")
    private Integer userType;

    @Schema(description = "关联公司ID（公司类型时有效）")
    private Long companyId;

    @Schema(description = "所属部门ID")
    private Long departmentId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
