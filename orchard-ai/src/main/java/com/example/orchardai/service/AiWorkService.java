package com.example.orchardai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardai.dto.AiWorkCreateDto;
import com.example.orchardai.dto.AiWorkQuery;
import com.example.orchardai.dto.AiWorkUpdateDto;
import com.example.orchardai.dto.AiWorkVo;
import com.example.orchardai.entity.AiWork;
import com.example.orchardai.enums.WorkStatusEnum;
import com.example.orchardcommon.result.PageResult;

public interface AiWorkService extends IService<AiWork> {

    AiWorkVo create(AiWorkCreateDto dto);

    AiWorkVo getDetail(Long id);

    PageResult<AiWorkVo> listByUser(AiWorkQuery query);

    void update(Long id, AiWorkUpdateDto dto);

    void updateStatus(Long id, WorkStatusEnum status);

    void delete(Long id);
}
