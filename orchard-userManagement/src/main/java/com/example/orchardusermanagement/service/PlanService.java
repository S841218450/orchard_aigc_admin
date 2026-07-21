package com.example.orchardusermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardusermanagement.entity.Plan;
import com.example.orchardusermanagement.dto.PlanDto;

public interface PlanService extends IService<Plan> {

    void add(PlanDto dto);

    void update(Long id, PlanDto dto);
}
