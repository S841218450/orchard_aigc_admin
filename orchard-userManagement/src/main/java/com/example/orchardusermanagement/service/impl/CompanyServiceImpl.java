package com.example.orchardusermanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardusermanagement.mapper.CompanyMapper;
import com.example.orchardusermanagement.entity.Company;
import com.example.orchardusermanagement.service.CompanyService;
import com.example.orchardusermanagement.dto.CompanyDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    @Override
    public void add(CompanyDto dto) {
        Company company = new Company();
        BeanUtils.copyProperties(dto, company);
        company.setId(SnowflakeUtils.nextId(BizCodeEnum.COMPANY));
        company.setStatus(1);
        company.setCreateTime(LocalDateTime.now());
        save(company);
    }

    @Override
    public void update(Long id, CompanyDto dto) {
        Company company = getById(id);
        if (company == null) {
            throw new RuntimeException("公司不存在");
        }
        BeanUtils.copyProperties(dto, company);
        company.setUpdateTime(LocalDateTime.now());
        updateById(company);
    }
}
