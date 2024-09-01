package com.sample.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.sample.demo.entity.Tutorial;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8081")
public class TutorialController {

	public List<Tutorial> tutorialList = new ArrayList<>();

	@PostMapping("/tutorials")
	public Tutorial addTutorial(@RequestBody Tutorial tutorial) {
		tutorialList.add(tutorial);
		return tutorial;
	}

	@PutMapping("/tutorials/{tutorialId}")
	public Tutorial updateTutorial(@PathVariable String tutorialId, @RequestBody Tutorial tutorial) {
		tutorialList.stream()
				.filter(item -> item.getId().equals(tutorialId))
				.peek(item -> {
					item.setTitle(tutorial.getTitle());
					item.setPublished(tutorial.isPublished());
					item.setDescription(tutorial.getDescription());
				})
				.findFirst()
				.orElse(null);
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
