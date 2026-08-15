package com.example.orchardai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.orchardai.dto.AiAssetRow;
import com.example.orchardai.entity.AiAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI素材Mapper：单表操作用 MyBatis-Plus，联表查询在 resources/mapper/AiAssetMapper.xml
 */
@Mapper
public interface AiAssetMapper extends BaseMapper<AiAsset> {

    /**
     * 分页联查作者昵称/头像（tag 入参需为 JSON 精确值，如 "人像"）
     */
    Page<AiAssetRow> selectPageWithAuthor(Page<?> page,
                                          @Param("type") String type,
                                          @Param("keyword") String keyword,
                                          @Param("tag") String tag);

    /**
     * 单条联查作者昵称/头像
     */
    AiAssetRow selectDetailWithAuthor(@Param("id") Long id);
}
