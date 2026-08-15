package com.example.orchardai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orchardai.dto.AiAssetCreateDto;
import com.example.orchardai.dto.AiAssetQuery;
import com.example.orchardai.dto.AiAssetVo;
import com.example.orchardai.entity.AiAsset;
import com.example.orchardcommon.result.PageResult;

public interface AiAssetService extends IService<AiAsset> {

    AiAssetVo create(AiAssetCreateDto dto);

    AiAssetVo getDetail(Long id);

    PageResult<AiAssetVo> page(AiAssetQuery query);

    void like(Long id);

    void delete(Long id);
}
