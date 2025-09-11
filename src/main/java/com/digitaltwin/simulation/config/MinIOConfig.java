package com.digitaltwin.simulation.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MinIOConfig {

    @Bean
    public MinioClient minioClient(MinIOProperties minIOProperties) {
        return MinioClient.builder()
                .endpoint(minIOProperties.getEndpoint())
                .credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey())
                .region(minIOProperties.getRegion())
                .build();
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "minio")
    public static class MinIOProperties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String region;
    }
}