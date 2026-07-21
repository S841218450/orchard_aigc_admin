package com.example.orchardusermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "公司DTO")
public class CompanyDto {

    @NotBlank(message = "公司名称不能为空")
    @Schema(description = "公司名称")
    private String name;

    @Schema(description = "统一社会信用代码")
    private String creditCode;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "地址")
    private String address;
}
