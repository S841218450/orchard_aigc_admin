package com.example.orchardusermanagement.controller;

import com.example.orchardcommon.result.Result;
import com.example.orchardusermanagement.dto.CompanyDto;
import com.example.orchardusermanagement.entity.Company;
import com.example.orchardusermanagement.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "公司管理")
@RestController
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @Operation(summary = "新增公司")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CompanyDto dto) {
        companyService.add(dto);
        return Result.ok();
    }

    @Operation(summary = "更新公司")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CompanyDto dto) {
        companyService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除公司")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        companyService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取公司详情")
    @GetMapping("/{id}")
    public Result<Company> getById(@PathVariable Long id) {
        Company company = companyService.getById(id);
        return Result.ok(company);
    }
}
