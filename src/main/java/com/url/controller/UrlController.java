package com.url.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.dto.ApiResponse;
import com.url.dto.UrlRequest;
import com.url.models.UrlMapping;
import com.url.models.User;
import com.url.repository.UserRepo;
import com.url.service.UrlMappingService;
import com.url.utils.JwtUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/v1/url/")
public class UrlController {
	private UrlMappingService url_service;
	private UserRepo user_repo;
	private JwtUtils jwtUtils;

	@Value("${base.app.url}")
	private String baseUrl;

	public UrlController(UrlMappingService url_service, UserRepo user_repo, JwtUtils jwtUtils) {
		this.url_service = url_service;
		this.user_repo = user_repo;
		this.jwtUtils = jwtUtils;
	}

//	POST /api/url/shorten

	@PostMapping("shorten")
	ResponseEntity<ApiResponse<HashMap<String, String>>> shortenUrl(@RequestBody @Valid UrlRequest request,
			@RequestHeader("Authorization") String token) {

		String username = jwtUtils.extractUsername(token.split("Bearer ")[1]);
//		String username = "Ravi Kumar";
		System.out.println("username" + username);

		User user = user_repo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

		UrlMapping mapping = url_service.shortenUrl(request, user);

		HashMap<String, String> response = new HashMap<String, String>();

		String newUrl = baseUrl + "/api/v1/url/r/" + mapping.getShortUrl();
		response.put("shortUrl", newUrl);
		response.put("expires at", mapping.getExpiresAt().toString());

		return new ResponseEntity<ApiResponse<HashMap<String, String>>>(
				new ApiResponse<HashMap<String, String>>("URL generated successfully", true, response),
				HttpStatus.CREATED);
	}

//	GET /r/{shortCode}
	@GetMapping("r/{shortCode}")
	ResponseEntity<ApiResponse<HashMap<String, String>>> redirectUrl(@PathVariable String shortCode)
			throws IOException {

		url_service.getOriginalUrl(shortCode);
		HashMap<String, String> response = new HashMap<String, String>();
		return new ResponseEntity<ApiResponse<HashMap<String, String>>>(
				new ApiResponse<HashMap<String, String>>("URL generated successfully", true, response),
				HttpStatus.CREATED);
	}

//	GET /api/user/{userId}/urls

	@GetMapping("all/{userId}")
	ResponseEntity<ApiResponse<List<UrlMapping>>> listAllUrls(@PathVariable Integer userId) {

		List<UrlMapping> urlMappings = url_service.getAllUrls(userId);

		return new ResponseEntity<ApiResponse<List<UrlMapping>>>(
				new ApiResponse<List<UrlMapping>>("URL's fetched successfully", true, urlMappings), HttpStatus.OK);
	}

//	DELETE /api/url/{id}
	@DeleteMapping("{id}")
	ResponseEntity<ApiResponse<String>> removeUrl(@PathVariable Integer id,
			@RequestHeader("Authorization") String token) {

		String username = jwtUtils.extractUsername(token.split("Bearer ")[1]);

		User user = user_repo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
		return url_service.removeUrl(id, user);
	}
}
