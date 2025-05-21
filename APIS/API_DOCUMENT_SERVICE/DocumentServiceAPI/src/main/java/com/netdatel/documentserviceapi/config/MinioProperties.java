package com.netdatel.documentserviceapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private boolean secure;
    private boolean enabled;
}