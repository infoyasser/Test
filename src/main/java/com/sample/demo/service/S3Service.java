package com.sample.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.demo.entity.Tutorial;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class S3Service {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String bucketName = "sample-springboot3-tutorial "; // Replace with your bucket name
    private final S3Client s3Client;

    public S3Service() {
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTHEAST_1) // Ensure the correct region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();  // Default credentials provider chain
    }

    public void uploadFile(String fileName, byte[] fileContent) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
    }

    public InputStream downloadFile(String fileName) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    public void uploadTutorialList(String fileName, List<Tutorial> tutorialList) throws IOException {
        try {
            String jsonContent = objectMapper.writeValueAsString(tutorialList);
            uploadFile(fileName, jsonContent.getBytes());
        } catch (JsonProcessingException e) {
            throw new IOException("Error converting tutorial list to JSON", e);
        }
    }

    public List<Tutorial> loadTutorialList(String fileName) throws IOException {
        InputStream fileContent = downloadFile(fileName);
        return objectMapper.readValue(fileContent, new TypeReference<List<Tutorial>>() {});
    }
}


