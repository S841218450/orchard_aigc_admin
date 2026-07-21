package com.example.orchardusermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardusermanagement.entity.Company;
import com.example.orchardusermanagement.dto.CompanyDto;

public interface CompanyService extends IService<Company> {

    void add(CompanyDto dto);

    void update(Long id, CompanyDto dto);

}
