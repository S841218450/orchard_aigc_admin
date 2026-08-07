package com.example.orchardfile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "统一上传DTO（folderId!=null则写入文件系统，否则只上传COS返回URL）")
public class FileUnifiedUploadDto {

    @Schema(description = "用户ID（未登录时传递，仅 folderId 非空时生效）")
    private Long userId;

    @Schema(description = "文件夹ID：传了（含0根目录）则写入文件系统file_record；不传则只上传COS返回URL")
    private Long folderId;

    @Schema(description = "单个base64内容（单文件base64上传）")
    private String base64;

    @Schema(description = "文件名（单文件base64/url上传时用）")
    private String fileName;

    @Schema(description = "批量base64文件列表")
    private List<FileUploadBase64Dto> base64List;

    @Schema(description = "单个url（单文件url上传）")
    private String url;

    @Schema(description = "批量url文件列表")
    private List<FileUploadUrlDto> urlList;
}
