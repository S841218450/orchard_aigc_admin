package com.example.orchardusermanagement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_department")
@Schema(description = "部门")
public class Department extends baseEntity {

    @Schema(description = "所属公司ID")
    private Long companyId;

    @Schema(description = "上级部门ID（0表示顶级部门）")
    private Long parentId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @TableField(exist = false)
    private List<Department> children;
}
