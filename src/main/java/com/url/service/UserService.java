package com.url.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.url.dto.ApiResponse;
import com.url.dto.UserRegister;
import com.url.models.User;
import com.url.repository.UserRepo;
import com.url.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Service
public class UserService {
	private UserRepo user_repo;
	private JwtUtils jwtUtils;
	private PasswordEncoder passEncoder;
	private RedisTemplate<String, Object> redisTemplate;

	public UserService(UserRepo user_repo, JwtUtils jwtUtils, PasswordEncoder passEncoder,
			RedisTemplate<String, Object> redisTemplate) {
		this.user_repo = user_repo;
		this.jwtUtils = jwtUtils;
		this.passEncoder = passEncoder;
		this.redisTemplate = redisTemplate;
	}

	public ResponseEntity<ApiResponse<String>> registerUser(UserRegister user) {

		User newUser = new User();
		newUser.setPassword(passEncoder.encode(user.getPassword()));
		newUser.setUsername(user.getUsername());

		User savedUser = user_repo.save(newUser);

		String token = jwtUtils.generateToken(user.getUsername(), savedUser.getId());
		return new ResponseEntity<ApiResponse<String>>(
				new ApiResponse<String>("", true, "User registered successfully with token - " + token),
				HttpStatus.CREATED);
	}

	public ResponseEntity<ApiResponse<String>> loginUser(@Valid UserRegister user) {
		User dbUser = user_repo.findByUsername(user.getUsername())
				.orElseThrow(() -> new RuntimeException("Invalid User or Credentials"));

		if (!passEncoder.matches(user.getPassword(), dbUser.getPassword())) {
			throw new RuntimeException("Invalid User or Credentials");
		}

//		get the token and send
		String token = jwtUtils.generateToken(user.getUsername(), dbUser.getId());

		System.out.println("Toekn" + token);
		return new ResponseEntity<ApiResponse<String>>(
				new ApiResponse<String>("", true, "User logged in successfully with token - " + token),
				HttpStatus.CREATED);
	}

	public ResponseEntity<ApiResponse<String>> logOutUser(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String authHeader = request.getHeader("Authorization");

		String token = null;
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
		}
		redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofMinutes(30));
		return new ResponseEntity<ApiResponse<String>>(
				new ApiResponse<String>("User logged out successfully", true, null), HttpStatus.ACCEPTED);
	}

}
