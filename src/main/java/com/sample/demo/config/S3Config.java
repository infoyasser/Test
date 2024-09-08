package com.sample.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_SOUTHEAST_1) // Adjust to your bucket's region
                .credentialsProvider(DefaultCredentialsProvider.create())  // Ensure Lambda IAM role is used
                .build(); // DefaultCredentialsProvider will automatically work in Lambda
    }
}
