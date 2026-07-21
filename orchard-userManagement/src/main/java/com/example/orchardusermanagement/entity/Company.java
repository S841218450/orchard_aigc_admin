package com.example.orchardusermanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_company")
@Schema(description = "公司")
public class Company extends baseEntity {

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

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;
}
