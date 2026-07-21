package com.example.orchardusermanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardusermanagement.entity.Plan;
import com.example.orchardusermanagement.mapper.PlanMapper;
import com.example.orchardusermanagement.service.PlanService;
import com.example.orchardusermanagement.dto.PlanDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {

    @Override
    public void add(PlanDto dto) {
        Plan plan = new Plan();
        BeanUtils.copyProperties(dto, plan);
        plan.setId(SnowflakeUtils.nextId(BizCodeEnum.PLAN));
        plan.setStatus(1);
        plan.setCreateTime(LocalDateTime.now());
        save(plan);
    }

    @Override
    public void update(Long id, PlanDto dto) {
        Plan plan = getById(id);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }
        BeanUtils.copyProperties(dto, plan);
        plan.setUpdateTime(LocalDateTime.now());
        updateById(plan);
    }
}
