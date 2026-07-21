package com.example.orchardauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定第三方平台 DTO
 */
@Data
@Schema(description = "绑定第三方平台")
public class BindOAuthDto {

    @NotBlank(message = "第三方平台类型不能为空")
    @Schema(description = "第三方平台：wechat/alipay/github")
    private String oauthType;

    @NotBlank(message = "授权码不能为空")
    @Schema(description = "授权码")
    private String code;

    @Schema(description = "状态码")
    private String state;
}
