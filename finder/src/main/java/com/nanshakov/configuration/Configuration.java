package com.nanshakov.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

@org.springframework.context.annotation.Configuration

@Import({Jedis.class, Kafka.class})
public class Configuration {

    @Value("${endpoint}")
    private String endpoint;
    @Value("${port}")
    private String port;
    @Value("${accessKey}")
    private String accessKey;
    @Value("${secretKey}")
    private String secretKey;

    @Bean
    MinioClient minioClient() throws InvalidPortException, InvalidEndpointException {
        return new MinioClient(endpoint, port, accessKey, secretKey);
    }
}
