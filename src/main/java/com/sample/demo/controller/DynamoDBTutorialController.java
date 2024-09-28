package com.sample.demo.controller;

import com.sample.demo.entity.Tutorial;
import com.sample.demo.service.DynamoDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v2")
@CrossOrigin(origins = "http://localhost:8081")
public class DynamoDBTutorialController {
    @Autowired
    private DynamoDBService dynamoDBService;
    public List<Tutorial> tutorialList = new ArrayList<>();

    @PostMapping("/tutorials")
    public Tutorial addTutorial(@RequestBody Tutorial tutorial) {
        dynamoDBService.saveTutorial(tutorial);
        return tutorial;
    }

    @PutMapping("/tutorials/{tutorialId}")
    public Tutorial updateTutorial(@PathVariable String tutorialId, @RequestBody Tutorial tutorial) {
        // Same logic as before for updating
        tutorial.setId(tutorialId);
        dynamoDBService.saveTutorial(tutorial);
        return tutorial;
    }

    @DeleteMapping("/tutorials/{tutorialId}")
    public boolean deleteTutorial(@PathVariable String tutorialId) {
        dynamoDBService.deleteTutorial(tutorialId);
        return true;
    }

    @GetMapping("/tutorials/{tutorialId}")
    public Tutorial getTutorial(@PathVariable String tutorialId) {
        return dynamoDBService.getTutorial(tutorialId);
    }

    @GetMapping("/tutorials")
    public List<Tutorial> getTutorials() {
        return dynamoDBService.getAllTutorials();
    }
}
