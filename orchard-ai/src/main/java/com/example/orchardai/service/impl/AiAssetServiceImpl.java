package com.example.orchardai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardai.dto.AiAssetCreateDto;
import com.example.orchardai.dto.AiAssetQuery;
import com.example.orchardai.dto.AiAssetRow;
import com.example.orchardai.dto.AiAssetVo;
import com.example.orchardai.entity.AiAsset;
import com.example.orchardai.entity.AiAssetLike;
import com.example.orchardai.entity.AiWork;
import com.example.orchardai.enums.WorkStatusEnum;
import com.example.orchardai.mapper.AiAssetLikeMapper;
import com.example.orchardai.mapper.AiAssetMapper;
import com.example.orchardai.service.AiAssetService;
import com.example.orchardai.service.AiWorkService;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardcommon.exception.BizException;
import com.example.orchardcommon.result.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssetServiceImpl extends ServiceImpl<AiAssetMapper, AiAsset> implements AiAssetService {

    private final AiAssetLikeMapper aiAssetLikeMapper;
    private final AiWorkService aiWorkService;
    private final ObjectMapper objectMapper;

    @Override
    public AiAssetVo create(AiAssetCreateDto dto) {
        Long userId = getCurrentUserId();
        AiWork work = aiWorkService.getById(dto.getWorkId());
        if (work == null) {
            throw new BizException("作品不存在");
        }
        if (!work.getUserId().equals(userId)) {
            throw new BizException("无权收录该作品");
        }
        if (work.getStatus() != WorkStatusEnum.COMPLETED.getCode()) {
            throw new BizException("仅已完成的作品可收录为素材");
        }
        if (!StringUtils.hasText(work.getResultUrl())) {
            throw new BizException("作品暂无可用素材地址");
        }

        AiAsset asset = new AiAsset();
        asset.setId(SnowflakeUtils.nextId(BizCodeEnum.ASSET));
        asset.setUserId(userId);
        asset.setType(work.getType());
        asset.setPrompt(work.getPrompt());
        asset.setParams(work.getParams());
        asset.setUrl(work.getResultUrl());
        asset.setLikeCount(0);
        asset.setCreateTime(LocalDateTime.now());
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            try {
                asset.setTags(objectMapper.writeValueAsString(dto.getTags()));
            } catch (JsonProcessingException e) {
                log.error("序列化tags失败", e);
            }
        }

        save(asset);
        AiAssetRow row = baseMapper.selectDetailWithAuthor(asset.getId());
        return toVo(row, false);
    }

    @Override
    public AiAssetVo getDetail(Long id) {
        AiAssetRow row = baseMapper.selectDetailWithAuthor(id);
        if (row == null) {
            throw new BizException("素材不存在");
        }
        return toVo(row, isLiked(id, getCurrentUserId()));
    }

    @Override
    public PageResult<AiAssetVo> page(AiAssetQuery query) {
        Long currentUserId = getCurrentUserId();

        // 标签按 JSON 精确值匹配（入参包上引号）
        String tagParam = StringUtils.hasText(query.getTag()) ? "\"" + query.getTag() + "\"" : null;

        Page<AiAssetRow> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<AiAssetRow> result = baseMapper.selectPageWithAuthor(page,
                query.getType(), query.getQuery(), tagParam);

        List<Long> assetIds = result.getRecords().stream().map(AiAssetRow::getId).collect(Collectors.toList());
        Set<Long> likedIds = likedSet(assetIds, currentUserId);

        return PageResult.of(result, row -> toVo(row, likedIds.contains(row.getId())));
    }

    @Override
    @Transactional
    public AiAssetVo like(Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }
        AiAsset asset = getById(id);
        if (asset == null) {
            throw new BizException("素材不存在");
        }

        AiAssetLike like = aiAssetLikeMapper.selectOne(new LambdaQueryWrapper<AiAssetLike>()
                .eq(AiAssetLike::getAssetId, id)
                .eq(AiAssetLike::getUserId, userId));

        int likeCount = asset.getLikeCount() == null ? 0 : asset.getLikeCount();
        if (like == null) {
            // 未点赞 → 点赞
            AiAssetLike newLike = new AiAssetLike();
            newLike.setId(SnowflakeUtils.nextId(BizCodeEnum.ASSET));
            newLike.setAssetId(id);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            aiAssetLikeMapper.insert(newLike);
            asset.setLikeCount(likeCount + 1);
        } else {
            // 已点赞 → 取消点赞
            aiAssetLikeMapper.deleteById(like.getId());
            asset.setLikeCount(Math.max(0, likeCount - 1));
        }
        asset.setUpdateTime(LocalDateTime.now());
        updateById(asset);

        AiAssetRow row = baseMapper.selectDetailWithAuthor(id);
        return toVo(row, !likedSet(List.of(id), userId).isEmpty());
    }

    @Override
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        AiAsset asset = getById(id);
        if (asset == null) {
            throw new BizException("素材不存在");
        }
        if (!asset.getUserId().equals(userId)) {
            throw new BizException("无权删除该素材");
        }
        removeById(id);
    }

    private AiAssetVo toVo(AiAssetRow row, boolean liked) {
        AiAssetVo vo = new AiAssetVo();
        vo.setId(row.getId());
        vo.setUserId(row.getUserId());
        vo.setAuthorName(row.getAuthorName());
        vo.setType(row.getType());
        vo.setPrompt(row.getPrompt());
        vo.setParams(row.getParams());
        vo.setUrl(row.getUrl());
        vo.setLikeCount(row.getLikeCount());
        vo.setLiked(liked);
        vo.setCreateTime(row.getCreateTime());

        if (row.getTags() != null) {
            try {
                vo.setTags(objectMapper.readValue(row.getTags(), new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.error("反序列化tags失败", e);
            }
        }
        return vo;
    }

    /**
     * 批量查询当前用户已点赞的素材ID集合
     */
    private Set<Long> likedSet(List<Long> assetIds, Long userId) {
        if (userId == null || assetIds == null || assetIds.isEmpty()) {
            return Collections.emptySet();
        }
        return aiAssetLikeMapper.selectList(new LambdaQueryWrapper<AiAssetLike>()
                        .in(AiAssetLike::getAssetId, assetIds)
                        .eq(AiAssetLike::getUserId, userId))
                .stream()
                .map(AiAssetLike::getAssetId)
                .collect(Collectors.toSet());
    }

    private boolean isLiked(Long assetId, Long userId) {
        return likedSet(List.of(assetId), userId).contains(assetId);
    }

    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return (Long) attributes.getRequest().getAttribute("userId");
    }
}
