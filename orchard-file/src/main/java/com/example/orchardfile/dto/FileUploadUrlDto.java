package com.example.orchardfile.dto;

import lombok.Data;

@Data
public class FileUploadUrlDto {
    private String url;
    private String fileName;
    private Long userId;
    private Long folderId;
}