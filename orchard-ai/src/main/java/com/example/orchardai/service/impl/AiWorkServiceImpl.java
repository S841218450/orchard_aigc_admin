package com.example.orchardai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.orchardai.dto.AiWorkCreateDto;
import com.example.orchardai.dto.AiWorkQuery;
import com.example.orchardai.dto.AiWorkUpdateDto;
import com.example.orchardai.dto.AiWorkVo;
import com.example.orchardai.dto.ImageItem;
import com.example.orchardai.entity.AiWork;
import com.example.orchardai.enums.WorkStatusEnum;
import com.example.orchardai.mapper.AiWorkMapper;
import com.example.orchardai.service.AiWorkService;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardcommon.exception.BizException;
import com.example.orchardcommon.result.PageResult;
import com.example.orchardfile.config.CosConfig;
import com.example.orchardfile.service.FileUploadService;
import com.example.orchardfile.vo.FileUploadVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWorkServiceImpl extends ServiceImpl<AiWorkMapper, AiWork> implements AiWorkService {

    private final ObjectMapper objectMapper;

    private final FileUploadService fileUploadService;

    private final CosConfig cosConfig;

    @Override
    public AiWorkVo create(AiWorkCreateDto dto) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long userId = (Long) attributes.getRequest().getAttribute("userId");

        AiWork work = new AiWork();
        work.setId(SnowflakeUtils.nextId(BizCodeEnum.WORK));
        work.setUserId(userId);
        work.setType(dto.getType());
        work.setPrompt(dto.getPrompt());
        work.setModel(dto.getModel() != null ? dto.getModel() : "default");
        work.setStatus(0);
        work.setCreateTime(LocalDateTime.now());

        if (dto.getParams() != null) {
            try {
                work.setParams(objectMapper.writeValueAsString(dto.getParams()));
            } catch (JsonProcessingException e) {
                log.error("序列化params失败", e);
            }
        }

        if (dto.getOriginImageList() != null) {
            try {
                work.setOriginImageList(objectMapper.writeValueAsString(dto.getOriginImageList()));
            } catch (JsonProcessingException e) {
                log.error("序列化originImageList失败", e);
            }
        }

        save(work);
        return toVo(work);
    }

    @Override
    public AiWorkVo getDetail(Long id) {
        AiWork work = getById(id);
        if (work == null) {
            throw new BizException("作品不存在");
        }
        return toVo(work);
    }

    @Override
    public PageResult<AiWorkVo> listByUser(AiWorkQuery query) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long userId = (Long) attributes.getRequest().getAttribute("userId");

        Page<AiWork> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<AiWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiWork::getUserId, userId)
               .eq(StringUtils.hasText(query.getType()), AiWork::getType, query.getType())
               .like(StringUtils.hasText(query.getQuery()), AiWork::getPrompt, query.getQuery())
               .orderByDesc(AiWork::getCreateTime);

        Page<AiWork> result = page(page, wrapper);
        return PageResult.of(result, this::toVo);
    }

    @Override
    public void delete(Long id) {
        AiWork work = getById(id);
        if (work == null) {
            throw new BizException("作品不存在");
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long userId = (Long) attributes.getRequest().getAttribute("userId");
        if (!work.getUserId().equals(userId)) {
            throw new BizException("无权删除该作品");
        }
        removeById(id);
    }

    @Override
    public AiWorkVo update(Long id, AiWorkUpdateDto dto) {
        AiWork work = getById(id);
        if (work == null) {
            throw new BizException("作品不存在");
        }
        if (dto.getResultUrl() != null) {
            work.setResultUrl(dto.getResultUrl());
        }
        if (dto.getPrompt() != null) {
            work.setPrompt(dto.getPrompt().strip());
        }
        if (dto.getDataList() != null) {
            try {
                for (ImageItem item : dto.getDataList()) {
                    if (!StringUtils.hasText(item.getUrl())) {
                        throw new BizException("dataList中图片url不能为空");
                    }
                    // 非本系统COS地址：服务端自动抓取并转存到COS，避免外部链接失效，省去"先上传再修改"的两步调用
                    if (!isCosUrl(item.getUrl())) {
                        FileUploadVo uploadVo = fileUploadService.uploadFileByUrl(item.getUrl(), null, work.getUserId(), null);
                        item.setUrl(uploadVo.getFileUrl());
                    }
                    // id缺失自动生成，保证每条数据都有文件标识
                    if (item.getId() == null) {
                        item.setId(SnowflakeUtils.nextId(BizCodeEnum.WORK_IMAGE));
                    }
                }
                // 追加模式：新图片拼接在已有图片之后，而不是整表覆盖
                List<ImageItem> merged = parseImageList(work.getDataList());
                merged.addAll(dto.getDataList());
                work.setDataList(objectMapper.writeValueAsString(merged));
                // 自动将最后一张设为resultUrl
                if (!merged.isEmpty()) {
                    work.setResultUrl(merged.getLast().getUrl());
                }
            } catch (JsonProcessingException e) {
                log.error("序列化dataList失败", e);
            }
        }
        work.setUpdateTime(LocalDateTime.now());
        updateById(work);
        return toVo(work);
    }

    @Override
    public void updateStatus(Long id, WorkStatusEnum status) {
        AiWork work = getById(id);
        if (work == null) {
            throw new BizException("作品不存在");
        }
        if (status.getCode() == work.getStatus()) {
            throw new BizException("已是当前状态");
        }
        work.setStatus(status.getCode());
        work.setOperationData(null); //清空待操作的数据
        work.setUpdateTime(LocalDateTime.now());
        updateById(work);
    }

    @Override
    public void updateStatusWithOperationData(Long id, WorkStatusEnum status, Object operationData) {
        AiWork work = getById(id);
        if (work == null) {
            throw new BizException("作品不存在");
        }
        work.setStatus(status.getCode());
        work.setUpdateTime(LocalDateTime.now());

        // 保存待操作数据
        if (operationData != null) {
            try {
                work.setOperationData(objectMapper.writeValueAsString(operationData));
            } catch (JsonProcessingException e) {
                log.error("序列化operationData失败", e);
            }
        }

        updateById(work);
    }

    /**
     * 解析作品已存的图片列表（兼容旧版纯URL字符串格式），解析失败返回空列表
     */
    private List<ImageItem> parseImageList(String dataListJson) {
        if (!StringUtils.hasText(dataListJson)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dataListJson, new TypeReference<List<ImageItem>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析已有图片数据失败，按空列表处理：{}", dataListJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 判断URL是否为当前系统的腾讯云COS地址（本系统已上传的图片无需再次转存）
     */
    private boolean isCosUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String defaultHost = cosConfig.getBucket() + ".cos." + cosConfig.getRegion() + ".myqcloud.com";
        if (url.contains(defaultHost)) {
            return true;
        }
        return StringUtils.hasText(cosConfig.getDomain()) && url.startsWith(cosConfig.getDomain());
    }

    private AiWorkVo toVo(AiWork work) {
        AiWorkVo vo = new AiWorkVo();
        vo.setId(work.getId());
        vo.setType(work.getType());
        vo.setPrompt(work.getPrompt());
        vo.setModel(work.getModel());
        vo.setStatus(work.getStatus());
        vo.setCreateTime(work.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        if (work.getParams() != null) {
            try {
                vo.setParams(objectMapper.readValue(work.getParams(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("反序列化params失败", e);
            }
        }
        vo.setResultUrl(work.getResultUrl());

        // 解析结果数据列表（兼容旧数据：元素为纯URL字符串）
        if (work.getDataList() != null) {
            try {
                JsonNode node = objectMapper.readTree(work.getDataList());
                if (node.isArray()) {
                    List<ImageItem> items = new ArrayList<>();
                    for (JsonNode n : node) {
                        ImageItem item;
                        if (n.isTextual()) {
                            // 旧数据：纯URL字符串
                            item = new ImageItem();
                            item.setUrl(n.asText());
                        } else {
                            item = objectMapper.treeToValue(n, ImageItem.class);
                        }
                        items.add(item);
                    }
                    vo.setDataList(items);
                }
            } catch (JsonProcessingException e) {
                log.error("反序列化dataList失败", e);
            }
        }

        // 解析原图数据列表
        if (work.getOriginImageList() != null) {
            try {
                vo.setOriginImageList(objectMapper.readValue(work.getOriginImageList(), new TypeReference<List<ImageItem>>() {}));
            } catch (JsonProcessingException e) {
                log.error("反序列化originImageList失败", e);
            }
        }

        // 解析待操作数据
        if (work.getOperationData() != null && work.getStatus()==WorkStatusEnum.PENDING_OPERATION.getCode()) {
            try {
                vo.setOperationData(objectMapper.readValue(work.getOperationData(), new TypeReference<Object>() {}));
            } catch (JsonProcessingException e) {
                log.error("反序列化operationData失败", e);
            }
        }

        return vo;
    }
}
