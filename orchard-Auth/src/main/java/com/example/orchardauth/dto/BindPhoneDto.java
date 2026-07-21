package com.example.orchardauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定手机号 DTO
 */
@Data
@Schema(description = "绑定手机号")
public class BindPhoneDto {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;
}
