package com.example.orchardusermanagement.controller;

import com.example.orchardcommon.result.Result;
import com.example.orchardusermanagement.dto.DepartmentDto;
import com.example.orchardusermanagement.entity.Department;
import com.example.orchardusermanagement.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Operation(summary = "新增部门")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody DepartmentDto dto) {
        departmentService.add(dto);
        return Result.ok();
    }

    @Operation(summary = "更新部门")
    @PutMapping("/update/{id}")
    public Result<Void> update(@RequestParam Long id, @Valid @RequestBody DepartmentDto dto) {
        departmentService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@RequestParam Long id) {
        departmentService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    public Result<List<Department>> getTree(@RequestParam Long companyId) {
        List<Department> tree = departmentService.getTree(companyId);
        return Result.ok(tree);
    }
}
