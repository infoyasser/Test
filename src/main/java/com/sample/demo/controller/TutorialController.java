package com.sample.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.awspring.cloud.s3.S3Exception;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sample.demo.entity.Tutorial;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8081")
public class TutorialController {

	//@Autowired
	//private S3Service s3Service;
	public List<Tutorial> tutorialList = new ArrayList<>();

//	@PostConstruct
//	public void init() {
//		try {
//			tutorialList = s3Service.loadTutorialList("tutorials.json");
//		} catch (S3Exception e) {
//			System.out.println ("Failed to download file from S3: {}");
//			System.out.println( e.getMessage());
//			throw e;
//		}
//		catch (IOException e) {
//			System.out.println( e.getMessage());
//			// Handle exception (e.g., log error or initialize an empty list)
//			tutorialList = new ArrayList<>();
//		}
//	}

	@PostMapping("/tutorials")
	public Tutorial addTutorial(@RequestBody Tutorial tutorial) throws IOException, IOException {
		tutorialList.add(tutorial);
		//s3Service.uploadTutorialList("tutorials.json", tutorialList); // Save list to S3
		return tutorial;
	}

	@PutMapping("/tutorials/{tutorialId}")
	public Tutorial updateTutorial(@PathVariable String tutorialId, @RequestBody Tutorial tutorial) throws IOException {
		tutorialList.stream()
				.filter(item -> item.getId().equals(tutorialId))
				.peek(item -> {
					item.setTitle(tutorial.getTitle());
					item.setPublished(tutorial.isPublished());
					item.setDescription(tutorial.getDescription());
				})
				.findFirst()
				.orElse(null);

		//s3Service.uploadTutorialList("tutorials.json", tutorialList); // Save updated list to S3
		return tutorial;
	}

	@DeleteMapping("/tutorials/{tutorialId}")
	public boolean deleteTutorial(@PathVariable String tutorialId) {
		return tutorialList.removeIf(item -> item.getId().equals(tutorialId));
	}

	@GetMapping("/tutorials/{tutorialId}")
	public Tutorial getTutorial(@PathVariable String tutorialId) {
		return tutorialList.stream()
				.filter(item -> item.getId().equals(tutorialId))
				.findFirst()
				.orElse(null);
	}

	@GetMapping("/tutorials")
	public List<Tutorial> getTutorials() {
		return tutorialList;
	}
}
