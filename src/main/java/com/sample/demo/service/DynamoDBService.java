package com.sample.demo.service;

import com.sample.demo.entity.Tutorial;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DynamoDBService {
    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "TutorialTable";

    public DynamoDBService() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.AP_SOUTHEAST_1) // Ensure correct region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void saveTutorial(Tutorial tutorial) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(tutorial.getId()).build());
        item.put("title", AttributeValue.builder().s(tutorial.getTitle()).build());
        item.put("description", AttributeValue.builder().s(tutorial.getDescription()).build());
        item.put("published", AttributeValue.builder().bool(tutorial.isPublished()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    public Tutorial getTutorial(String tutorialId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(tutorialId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        Map<String, AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();

        if (returnedItem != null && !returnedItem.isEmpty()) {
            Tutorial tutorial = new Tutorial();
            tutorial.setId(returnedItem.get("id").s());
            tutorial.setTitle(returnedItem.get("title").s());
            tutorial.setDescription(returnedItem.get("description").s());
            tutorial.setPublished(returnedItem.get("published").bool());
            return tutorial;
        }

        return null;
    }

    public List<Tutorial> getAllTutorials() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Map<String, AttributeValue>> items = response.items();

        List<Tutorial> tutorials = new ArrayList<>();
        for (Map<String, AttributeValue> item : items) {
            Tutorial tutorial = new Tutorial();
            tutorial.setId(item.get("id").s());
            tutorial.setTitle(item.get("title").s());
            tutorial.setDescription(item.get("description").s());
            tutorial.setPublished(item.get("published").bool());
            tutorials.add(tutorial);
        }
        return tutorials;
    }

    public void deleteTutorial(String tutorialId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(tutorialId).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(request);
    }
}
