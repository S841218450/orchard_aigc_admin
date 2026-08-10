package com.example.orchardai.controller;

import com.example.orchardai.dto.AiWorkCreateDto;
import com.example.orchardai.dto.AiWorkIdDto;
import com.example.orchardai.dto.AiWorkQuery;
import com.example.orchardai.dto.AiWorkUpdateDto;
import com.example.orchardai.dto.AiWorkVo;
import com.example.orchardai.enums.WorkStatusEnum;
import com.example.orchardai.service.AiWorkService;
import com.example.orchardcommon.annotation.PublicApi;
import com.example.orchardcommon.dto.IdDto;
import com.example.orchardcommon.result.PageResult;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI作品管理")
@RestController
@RequestMapping("/api/ai/work")
@RequiredArgsConstructor
public class AiWorkController {

    private final AiWorkService aiWorkService;

    @Operation(summary = "创建作品")
    @PostMapping("/create")
    public Result<AiWorkVo> create(@Valid @RequestBody AiWorkCreateDto dto) {
        return Result.ok(aiWorkService.create(dto));
    }

    @Operation(summary = "获取作品详情")
    @GetMapping("/detail")
    public Result<AiWorkVo> detail(@PathVariable Long id) {
        return Result.ok(aiWorkService.getDetail(id));
    }

    @Operation(summary = "获取当前用户作品列表（分页）")
    @PostMapping("/page")
    public Result<PageResult<AiWorkVo>> page(@RequestBody AiWorkQuery query) {
        return Result.ok(aiWorkService.listByUser(query));
    }

    @Operation(summary = "更新作品（图片/提示词等）")
    @PublicApi
    @PutMapping("/update")
    public Result<AiWorkVo> update(@RequestBody AiWorkUpdateDto dto) {
        return Result.ok(aiWorkService.update(dto.getId(), dto));
    }
    //作品状态相关（Agent端调用，无需登录鉴权）
    @PublicApi
    @Operation(summary = "更新作品状态为生成中")
    @PutMapping("/generating")
    public Result<Void> generating(@Valid @RequestBody IdDto dto) {
        aiWorkService.updateStatus(dto.getId(), WorkStatusEnum.GENERATING);
        return Result.ok();
    }

    @PublicApi
    @Operation(summary = "更新作品状态为已完成")
    @PutMapping("/completed")
    public Result<Void> completed(@Valid @RequestBody IdDto dto) {
        aiWorkService.updateStatus(dto.getId(), WorkStatusEnum.COMPLETED);
        return Result.ok();
    }

    @PublicApi
    @Operation(summary = "更新作品状态为失败")
    @PutMapping("/failed")
    public Result<Void> failed(@Valid @RequestBody AiWorkIdDto dto) {
        aiWorkService.updateStatus(dto.getId(), WorkStatusEnum.FAILED);
        return Result.ok();
    }

    @PublicApi
    @Operation(summary = "更新作品状态为待操作")
    @PutMapping("/pending")
    public Result<Void> pending(@RequestBody AiWorkUpdateDto dto) {
        aiWorkService.updateStatusWithOperationData(dto.getId(), WorkStatusEnum.PENDING_OPERATION, dto.getDataList());
        return Result.ok();
    }

    @Operation(summary = "删除作品")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody AiWorkIdDto dto) {
        aiWorkService.delete(dto.getId());
        return Result.ok();
    }
}
