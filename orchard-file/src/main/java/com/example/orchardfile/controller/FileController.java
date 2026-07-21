package com.example.orchardfile.controller;

import com.example.orchardcommon.result.Result;
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
    @PostMapping("/upload")
    public Result<FileUploadVo> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        FileUploadVo vo = fileUploadService.uploadFile(file, userId, folderId);
        return Result.ok(vo);
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
}
