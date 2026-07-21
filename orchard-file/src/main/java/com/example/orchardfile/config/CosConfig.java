package com.example.orchardfile.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cos")
public class CosConfig {

    /**
     * SecretId
     */
    private String secretId;

    /**
     * SecretKey
     */
    private String secretKey;

    /**
     * 区域（如：ap-guangzhou）
     */
    private String region;

    /**
     * 存储桶名称
     */
    private String bucket;

    /**
     * 访问域名（可选，自定义域名）
     */
    private String domain;

    /**
     * 最大文件大小（字节）- 默认 50MB
     */
    private Long maxFileSize = 50 * 1024 * 1024L;

    /**
     * 最小文件大小（字节）- 默认 1KB
     */
    private Long minFileSize = 1024L;

    /**
     * 分块上传阈值（字节）- 默认 8MB
     */
    private Long multipartThreshold = 8 * 1024 * 1024L;

    /**
     * 分块大小（字节）- 默认 2MB
     */
    private Long partSize = 2 * 1024 * 1024L;

    @Bean
    public COSClient cosClient() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(credentials, clientConfig);
    }
}
