package com.url.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.dto.ApiResponse;
import com.url.dto.UserRegister;
import com.url.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/v1/users/")
public class UserController {

	private UserService user_service;

	public UserController(UserService user_service) {
		this.user_service = user_service;
	}

	@PostMapping("register")
	ResponseEntity<ApiResponse<String>> registerUser(@RequestBody @Valid UserRegister user) {
		return user_service.registerUser(user);
	}

	@PostMapping("login")
	ResponseEntity<ApiResponse<String>> loginUser(@RequestBody @Valid UserRegister user) {
		return user_service.loginUser(user);
	}
	
	@PostMapping("logout")
	ResponseEntity<ApiResponse<String>> logOutUser(HttpServletRequest request) {
		return user_service.logOutUser(request);
	}
}
