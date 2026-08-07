package com.example.orchardai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.orchardai.dto.AiAssetRow;
import com.example.orchardai.entity.AiAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiAssetMapper extends BaseMapper<AiAsset> {

    String BASE_AUTHOR_SQL = "SELECT a.id, a.user_id, a.type, a.prompt, a.params, a.url, a.tags, a.like_count, a.create_time, u.nickname AS author_name "
            + "FROM t_ai_asset a LEFT JOIN t_user u ON a.user_id = u.id WHERE a.deleted = 0 ";

    /**
     * 分页联查作者昵称（tag 入参需为 JSON 精确值，如 "人像"）
     */
    @Select("<script>" + BASE_AUTHOR_SQL +
            "<if test='type != null and type != \"\"'> AND a.type = #{type}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND a.prompt LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "<if test='tag != null and tag != \"\"'> AND a.tags LIKE CONCAT('%', #{tag}, '%')</if>" +
            " ORDER BY a.create_time DESC" +
            "</script>")
    Page<AiAssetRow> selectPageWithAuthor(Page<?> page,
                                          @Param("type") String type,
                                          @Param("keyword") String keyword,
                                          @Param("tag") String tag);

    /**
     * 单条联查作者昵称
     */
    @Select(BASE_AUTHOR_SQL + "AND a.id = #{id}")
    AiAssetRow selectDetailWithAuthor(@Param("id") Long id);
}
