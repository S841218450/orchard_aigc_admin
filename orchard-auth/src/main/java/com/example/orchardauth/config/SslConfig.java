package com.example.orchardauth.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * SSL 配置（开发环境使用）
 * 解决 JDK 信任证书问题
 */
@Slf4j
@Configuration
public class SslConfig {

    @PostConstruct
    public void init() {
        try {
            // 创建信任所有证书的 TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            // 设置全局 SSL 上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLContext.setDefault(sslContext);

            // 设置全局主机名验证器
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            log.info("SSL 配置已初始化（开发模式：跳过证书验证）");
        } catch (Exception e) {
            log.warn("SSL 配置初始化失败", e);
        }
    }
}
