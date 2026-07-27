package com.example.orchardfile.controller;

import com.example.orchardcommon.annotation.PublicApi;
import com.example.orchardcommon.result.Result;
import com.example.orchardfile.dto.FileUploadBase64BatchDto;
import com.example.orchardfile.dto.FileUploadBase64Dto;
import com.example.orchardfile.dto.FileUploadUrlDto;
import com.example.orchardfile.dto.FileUploadUrlBatchDto;
import com.example.orchardfile.service.FileFolderService;
import com.example.orchardfile.service.FileUploadService;
import com.example.orchardfile.vo.FileDetailVo;
import com.example.orchardfile.vo.FileUploadVo;
import com.example.orchardfile.vo.FolderTreeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理 Controller
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;
    private final FileFolderService fileFolderService;

    @Operation(summary = "上传文件")
    @PublicApi
    @PostMapping("/upload")
    public Result<FileUploadVo> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(value = "folderId", required = false) Long folderId,
            HttpServletRequest request) {
        Long userId = getUserId(request, paramUserId);
        FileUploadVo vo = fileUploadService.uploadFile(file, userId, folderId);
        return Result.ok(vo);
    }

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
    public Result<List<FileUploadVo>> uploadFileBase64Batch(@RequestBody FileUploadBase64BatchDto dto, HttpServletRequest request) {
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

    @Operation(summary = "创建文件夹")
    @PostMapping("/folder/create")
    public Result<Long> createFolder(
            @RequestParam("folderName") String folderName,
            @RequestParam(value = "parentId", required = false) Long parentId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long folderId = fileFolderService.createFolder(folderName, parentId, userId);
        return Result.ok(folderId);
    }

    @Operation(summary = "获取文件夹树")
    @GetMapping("/folder/tree")
    public Result<List<FolderTreeVo>> getFolderTree(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FolderTreeVo> tree = fileFolderService.getFolderTree(userId);
        return Result.ok(tree);
    }

    @Operation(summary = "获取文件夹下的文件列表")
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
     * 获取用户ID：优先从request attribute获取（已登录），其次从参数获取（未登录）
     */
    private Long getUserId(HttpServletRequest request, Long paramUserId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = paramUserId;
        }
        return userId;
    }
}
