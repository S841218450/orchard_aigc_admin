package com.example.orchardai.controller;

import com.example.orchardai.dto.AiAssetCreateDto;
import com.example.orchardai.dto.AiAssetIdDto;
import com.example.orchardai.dto.AiAssetQuery;
import com.example.orchardai.dto.AiAssetVo;
import com.example.orchardai.service.AiAssetService;
import com.example.orchardcommon.annotation.PublicApi;
import com.example.orchardcommon.result.PageResult;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI素材管理")
@RestController
@RequestMapping("/api/ai/asset")
@RequiredArgsConstructor
public class AiAssetController {

    private final AiAssetService aiAssetService;

    @Operation(summary = "作品收录为素材")
    @PostMapping("/createAsset")
    public Result<AiAssetVo> create(@Valid @RequestBody AiAssetCreateDto dto) {
        return Result.ok(aiAssetService.create(dto));
    }

    @PublicApi
    @Operation(summary = "素材分页列表（首页案例/工作台参考）")
    @PostMapping("/getAssetList")
    public Result<PageResult<AiAssetVo>> page(@RequestBody AiAssetQuery query) {
        return Result.ok(aiAssetService.page(query));
    }

    @PublicApi
    @Operation(summary = "获取素材详情")
    @PostMapping("/getAssetDetail")
    public Result<AiAssetVo> detail(@Valid @RequestBody AiAssetIdDto dto) {
        return Result.ok(aiAssetService.getDetail(dto.getId()));
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/likeAsset")
    public Result<AiAssetVo> like(@Valid @RequestBody AiAssetIdDto dto) {
        return Result.ok(aiAssetService.like(dto.getId()));
    }

    @Operation(summary = "删除素材")
    @PostMapping("/deleteAsset")
    public Result<Void> delete(@Valid @RequestBody AiAssetIdDto dto) {
        aiAssetService.delete(dto.getId());
        return Result.ok();
    }
}
