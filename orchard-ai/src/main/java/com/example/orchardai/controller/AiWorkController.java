package com.example.orchardai.controller;

import com.example.orchardai.dto.AiWorkCreateDto;
import com.example.orchardai.dto.AiWorkQuery;
import com.example.orchardai.dto.AiWorkUpdateDto;
import com.example.orchardai.dto.AiWorkVo;
import com.example.orchardai.enums.WorkStatusEnum;
import com.example.orchardai.service.AiWorkService;
import com.example.orchardcommon.annotation.PublicApi;
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
    @GetMapping("/detail/{id}")
    public Result<AiWorkVo> detail(@PathVariable Long id) {
        return Result.ok(aiWorkService.getDetail(id));
    }

    @Operation(summary = "获取当前用户作品列表（分页）")
    @GetMapping("/list")
    public Result<PageResult<AiWorkVo>> list(@ModelAttribute AiWorkQuery query) {
        return Result.ok(aiWorkService.listByUser(query));
    }

    @Operation(summary = "更新作品")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AiWorkUpdateDto dto) {
        aiWorkService.update(id, dto);
        return Result.ok();
    }
    //作品状态相关（Agent端调用，无需登录鉴权）
    @PublicApi
    @Operation(summary = "更新作品状态为生成中")
    @PutMapping("/generating/{id}")
    public Result<Void> generating(@PathVariable Long id) {
        aiWorkService.updateStatus(id, WorkStatusEnum.GENERATING);
        return Result.ok();
    }

    @PublicApi
    @Operation(summary = "更新作品状态为已完成")
    @PutMapping("/completed/{id}")
    public Result<Void> completed(@PathVariable Long id) {
        aiWorkService.updateStatus(id, WorkStatusEnum.COMPLETED);
        return Result.ok();
    }

    @PublicApi
    @Operation(summary = "更新作品状态为失败")
    @PutMapping("/failed/{id}")
    public Result<Void> failed(@PathVariable Long id) {
        aiWorkService.updateStatus(id, WorkStatusEnum.FAILED);
        return Result.ok();
    }

    @PublicApi
    @Operation(summary = "更新作品状态为待操作")
    @PutMapping("/pending/{id}")
    public Result<Void> pending(@PathVariable Long id) {
        aiWorkService.updateStatus(id, WorkStatusEnum.PENDING_OPERATION);
        return Result.ok();
    }

    @Operation(summary = "删除作品")
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiWorkService.delete(id);
        return Result.ok();
    }
}
