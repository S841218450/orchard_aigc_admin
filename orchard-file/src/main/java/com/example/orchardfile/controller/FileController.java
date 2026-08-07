package com.example.orchardfile.controller;

import com.example.orchardcommon.annotation.PublicApi;
import com.example.orchardcommon.result.Result;
import com.example.orchardfile.dto.*;
import com.example.orchardfile.service.FileFolderService;
import com.example.orchardfile.service.FileUploadService;
import com.example.orchardfile.vo.FileDetailVo;
import com.example.orchardfile.vo.FileUploadVo;
import com.example.orchardfile.vo.FolderTreeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;

/**
 * 文件管理 Controller
 *
 * 上传规则：
 *   folderId != null（含 0 = 根目录）→ 写入 file_record 数据库，绑定到该目录；
 *   folderId == null（缺省）→ 只上传到腾讯云 COS，返回 fileUrl，不写数据库。
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;
    private final FileFolderService fileFolderService;

    // ===================== 上传接口（二进制文件） =====================

    @Operation(summary = "上传二进制文件（单）")
    @PublicApi
    @PostMapping("/upload")
    public Result<FileUploadVo> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "data", required = false) FileUploadDto dto,
            HttpServletRequest request) {
        dto = dto == null ? new FileUploadDto() : dto;
        Long userId = getUserId(request, dto.getUserId());
        FileUploadVo vo = fileUploadService.uploadFile(file, userId, dto.getFolderId());
        return Result.ok(vo);
    }

    @Operation(summary = "批量上传二进制文件")
    @PublicApi
    @PostMapping("/uploadBatch")
    public Result<List<FileUploadVo>> uploadFileBatch(
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart(value = "data", required = false) FileUploadDto dto,
            HttpServletRequest request) {
        dto = dto == null ? new FileUploadDto() : dto;
        Long userId = getUserId(request, dto.getUserId());
        List<FileUploadVo> results = fileUploadService.uploadFileBatch(files, userId, dto.getFolderId());
        return Result.ok(results);
    }

    // ===================== 上传接口（Base64 / URL） =====================

    @Operation(summary = "上传文件（Base64）")
    @PublicApi
    @PostMapping("/uploadFileByBase64")
    public Result<FileUploadVo> uploadFileBase64(@RequestBody FileUploadBase64Dto dto, HttpServletRequest request) {
        Long userId = getUserId(request, dto.getUserId());
        FileUploadVo vo = fileUploadService.uploadFileBase64(dto.getBase64(), dto.getFileName(), userId, dto.getFolderId());
        return Result.ok(vo);
    }

    @Operation(summary = "批量上传文件（Base64）")
    @PublicApi
    @PostMapping("/uploadFileByBase64Batch")
    public Result<List<FileUploadVo>> uploadFileBase64Batch(@Valid @RequestBody FileUploadBase64BatchDto dto, HttpServletRequest request) {
        Long userId = getUserId(request, dto.getUserId());
        List<FileUploadVo> results = fileUploadService.uploadFileBase64Batch(dto.getFiles(), userId, dto.getFolderId());
        return Result.ok(results);
    }

    @Operation(summary = "通过URL上传文件")
    @PublicApi
    @PostMapping("/uploadFileByUrl")
    public Result<FileUploadVo> uploadFileByUrl(@RequestBody FileUploadUrlDto dto, HttpServletRequest request) {
        Long userId = getUserId(request, dto.getUserId());
        FileUploadVo vo = fileUploadService.uploadFileByUrl(dto.getUrl(), dto.getFileName(), userId, dto.getFolderId());
        return Result.ok(vo);
    }

    @Operation(summary = "批量通过URL上传文件")
    @PublicApi
    @PostMapping("/uploadFileByUrlBatch")
    public Result<List<FileUploadVo>> uploadFileByUrlBatch(@RequestBody FileUploadUrlBatchDto dto, HttpServletRequest request) {
        Long userId = getUserId(request, dto.getUserId());
        List<FileUploadVo> results = fileUploadService.uploadFileByUrlBatch(dto.getFiles(), userId, dto.getFolderId());
        return Result.ok(results);
    }

    @Operation(summary = "统一上传（Base64/URL 单文件或批量，纯JSON Body；二进制请用 /upload 或 /uploadBatch）")
    @PublicApi
    @PostMapping("/unifiedUpload")
    public Result<List<FileUploadVo>> unifiedUpload(
            @RequestBody FileUnifiedUploadDto dto,
            HttpServletRequest request) {
        Long userId = getUserId(request, dto.getUserId());
        List<FileUploadVo> results = fileUploadService.unifiedUpload(dto, userId);
        return Result.ok(results);
    }

    // ===================== 文件目录接口 =====================

    @Operation(summary = "创建文件夹（需要写进文件系统目录体系时才需要）")
    @PostMapping("/folder/create")
    public Result<Long> createFolder(
            @Valid @RequestBody FolderCreateDto dto,
            HttpServletRequest request) {
        String folderName = dto.getFolderName();
        Long parentId = dto.getParentId();
        Long userId = (Long) request.getAttribute("userId");
        Long folderId = fileFolderService.createFolder(folderName, parentId, userId);
        return Result.ok(folderId);
    }

    @Operation(summary = "获取文件夹树（文件系统目录体系）")
    @GetMapping("/folder/tree")
    public Result<List<FolderTreeVo>> getFolderTree(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FolderTreeVo> tree = fileFolderService.getFolderTree(userId);
        return Result.ok(tree);
    }

    @Operation(summary = "获取文件夹下的文件列表（只展示 folderId!=null 写入的文件）")
    @GetMapping("/folder/files")
    public Result<List<FileDetailVo>> getFilesByFolder(
            @RequestParam(value = "folderId", required = false) Long folderId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FileDetailVo> files = fileFolderService.getFilesByFolder(folderId, userId);
        return Result.ok(files);
    }

    @Operation(summary = "删除文件夹")
    @DeleteMapping("/folder/{folderId}")
    public Result<Void> deleteFolder(
            @PathVariable Long folderId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        fileFolderService.deleteFolder(folderId, userId);
        return Result.ok();
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(
            @PathVariable Long fileId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        fileFolderService.deleteFile(fileId, userId);
        return Result.ok();
    }

    /**
     * 获取用户ID：优先从 request attribute 获取（已登录），其次从参数获取（未登录）。
     * 如果都没有，返回 null（COS 路径走 simple/yyyy/MM/dd）
     */
    private Long getUserId(HttpServletRequest request, Long paramUserId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = paramUserId;
        }
        return userId;
    }
}
