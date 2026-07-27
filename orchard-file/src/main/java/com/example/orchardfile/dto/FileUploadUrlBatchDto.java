package com.example.orchardfile.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileUploadUrlBatchDto {
    private List<FileUploadUrlDto> files;
    private Long userId;
    private Long folderId;
}