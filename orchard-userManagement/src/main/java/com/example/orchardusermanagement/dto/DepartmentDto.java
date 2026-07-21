package com.example.orchardusermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "部门DTO")
public class DepartmentDto {

    @NotNull(message = "公司ID不能为空")
    @Schema(description = "所属公司ID")
    private Long companyId;

    @Schema(description = "上级部门ID（0表示顶级部门）")
    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;
}
