package com.sample.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sample.demo.entity.User;

@RestController("/user")
public class UserController {

	public List<User> userList = new ArrayList<>();
	
	@PostMapping
	public User addUser( @RequestBody User user ) {
		userList.add(user);
		return user;
	}
	
	@PutMapping
	public User updateUser(@RequestParam String userId ,@RequestBody User user ) {
		Optional<User> updatedUser = userList.stream().filter(item -> item.getId().equals(userId)).findFirst();
		return user;
	}
	
	@DeleteMapping
	public boolean deleteUser( @RequestParam String userId ) {
		userList.removeIf(item -> item.getId().equals(userId));
		return true;
	}
	
	@GetMapping
	public User getUser( @RequestParam String userId ) {
		User user = null;
		user = userList.stream().filter(item -> item.getId().equals(userId)).findFirst().orElse(null);
		
		return user;
	}
}
