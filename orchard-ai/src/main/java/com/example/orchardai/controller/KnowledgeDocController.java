package com.example.orchardai.controller;

import com.example.orchardai.dto.*;
import com.example.orchardai.service.KnowledgeDocService;
import com.example.orchardai.service.KnowledgeFolderService;
import com.example.orchardai.vo.KnowledgeFolderTreeVo;
import com.example.orchardcommon.annotation.InternalApi;
import com.example.orchardcommon.dto.IdDto;
import com.example.orchardcommon.result.PageResult;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;

@Tag(name = "知识库文档管理")
@RestController
@RequestMapping("/api/ai/knowledge")
@RequiredArgsConstructor
public class KnowledgeDocController {

    private final KnowledgeDocService knowledgeDocService;
    private final KnowledgeFolderService knowledgeFolderService;

    @Operation(summary = "上传知识库文档（二进制文件 + 目录ID，一步到位）")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Result<KnowledgeDocVo> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId) {
        Long userId = getCurrentUserId();
        return Result.ok(knowledgeDocService.upload(file, folderId, userId));
    }

    @InternalApi
    @Operation(summary = "状态回调（Agent调用）")
    @PutMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody KnowledgeDocStatusUpdateDto dto) {
        knowledgeDocService.updateStatus(dto);
        return Result.ok();
    }

    @Operation(summary = "文档列表（按目录过滤 + 分页 + 关键字搜索）")
    @PostMapping("/list")
    public Result<PageResult<KnowledgeDocVo>> page(@RequestBody KnowledgeDocQueryDto query) {
        Long userId = getCurrentUserId();
        return Result.ok(knowledgeDocService.page(query, userId));
    }

    @Operation(summary = "删除文档")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody IdDto dto) {
        Long userId = getCurrentUserId();
        knowledgeDocService.delete(dto.getId(), userId);
        return Result.ok();
    }

    @Operation(summary = "重试向量入库（仅待处理/失败文档）")
    @PostMapping("/retry")
    public Result<Void> retry(@Valid @RequestBody IdDto dto) {
        Long userId = getCurrentUserId();
        knowledgeDocService.retry(dto.getId(), userId);
        return Result.ok();
    }

    @Operation(summary = "文档详情")
    @PostMapping("/detail")
    public Result<KnowledgeDocVo> detail(@Valid @RequestBody IdDto dto) {
        Long userId = getCurrentUserId();
        return Result.ok(knowledgeDocService.getById(dto.getId(), userId));
    }

    // ======================== 目录接口（与 file 模块同构） ========================

    @Operation(summary = "创建目录")
    @PostMapping("/folder/create")
    public Result<Long> createFolder(@Valid @RequestBody KnowledgeFolderCreateDto dto) {
        Long userId = getCurrentUserId();
        return Result.ok(knowledgeFolderService.createFolder(dto, userId));
    }

    @Operation(summary = "获取目录树")
    @PostMapping("/folder/tree")
    public Result<List<KnowledgeFolderTreeVo>> getFolderTree() {
        Long userId = getCurrentUserId();
        return Result.ok(knowledgeFolderService.getFolderTree(userId));
    }

    @Operation(summary = "删除目录")
    @PostMapping("/folder/delete")
    public Result<Void> deleteFolder(@Valid @RequestBody IdDto dto) {
        Long userId = getCurrentUserId();
        knowledgeFolderService.deleteFolder(dto.getId(), userId);
        return Result.ok();
    }

    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        Object userId = attributes.getRequest().getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }
}
