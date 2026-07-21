package com.example.orchardusermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardusermanagement.entity.Department;
import com.example.orchardusermanagement.mapper.DepartmentMapper;
import com.example.orchardusermanagement.service.DepartmentService;
import com.example.orchardusermanagement.dto.DepartmentDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public void add(DepartmentDto dto) {
        Department department = new Department();
        BeanUtils.copyProperties(dto, department);
        department.setId(SnowflakeUtils.nextId(BizCodeEnum.DEPARTMENT));
        department.setStatus(1);
        department.setCreateTime(LocalDateTime.now());
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        save(department);
    }

    @Override
    public void update(Long id, DepartmentDto dto) {
        Department department = getById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }
        BeanUtils.copyProperties(dto, department);
        department.setUpdateTime(LocalDateTime.now());
        updateById(department);
    }

    @Override
    public List<Department> getTree(Long companyId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getCompanyId, companyId)
               .eq(Department::getStatus, 1)
               .orderByAsc(Department::getSort);
        List<Department> allDepartments = list(wrapper);
        
        return buildTree(allDepartments, 0L);
    }

    private List<Department> buildTree(List<Department> allDepartments, Long parentId) {
        return allDepartments.stream()
                .filter(dept -> parentId.equals(dept.getParentId()))
                .peek(dept -> dept.setChildren(buildTree(allDepartments, dept.getId())))
                .collect(Collectors.toList());
    }
}
