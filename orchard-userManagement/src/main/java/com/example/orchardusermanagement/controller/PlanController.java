package com.example.orchardusermanagement.controller;

import com.example.orchardcommon.result.Result;
import com.example.orchardusermanagement.dto.PlanDto;
import com.example.orchardusermanagement.entity.Plan;
import com.example.orchardusermanagement.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "套餐管理")
@RestController
@RequestMapping("/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @Operation(summary = "新增套餐")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody PlanDto dto) {
        planService.add(dto);
        return Result.ok();
    }

    @Operation(summary = "更新套餐")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PlanDto dto) {
        planService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        planService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取套餐详情")
    @GetMapping("/{id}")
    public Result<Plan> getById(@PathVariable Long id) {
        Plan plan = planService.getById(id);
        return Result.ok(plan);
    }
}
