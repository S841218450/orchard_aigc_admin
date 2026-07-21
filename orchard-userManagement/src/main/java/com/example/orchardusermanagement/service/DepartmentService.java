package com.example.orchardusermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardusermanagement.entity.Department;
import com.example.orchardusermanagement.dto.DepartmentDto;

import java.util.List;

public interface DepartmentService extends IService<Department> {

    void add(DepartmentDto dto);

    void update(Long id, DepartmentDto dto);

    List<Department> getTree(Long companyId);
}
