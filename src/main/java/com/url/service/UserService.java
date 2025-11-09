package com.url.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;

import com.url.dto.ApiResponse;
import com.url.dto.UserRegister;
import com.url.models.User;
import com.url.repository.UserRepo;
import com.url.utils.JwtUtils;
import com.url.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Service
public class UserService {
	private UserRepo user_repo;
	private JwtUtils jwtUtils;
	private PasswordEncoder passEncoder;
	private RedisTemplate<String, Object> redisTemplate;
	private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1 * 60 * 1000; // 30 minutes
	private static final long REFRESH_TOKEN_EXPIRATION_TIME = 8 * 60 * 60 * 1000; // 8 Hours

	public UserService(UserRepo user_repo, JwtUtils jwtUtils, PasswordEncoder passEncoder,
			RedisTemplate<String, Object> redisTemplate) {
		this.user_repo = user_repo;
		this.jwtUtils = jwtUtils;
		this.passEncoder = passEncoder;
		this.redisTemplate = redisTemplate;
	}

	public ResponseEntity<ApiResponse<HashMap<String, String>>> registerUser(UserRegister user) {

		User newUser = new User();
		newUser.setPassword(passEncoder.encode(user.getPassword()));
		newUser.setUsername(user.getUsername());

		User savedUser = user_repo.save(newUser);

		String accessToken = jwtUtils.generateToken(user.getUsername(), savedUser.getId(),
				ACCESS_TOKEN_EXPIRATION_TIME);
		String refreshToken = jwtUtils.generateToken(user.getUsername(), savedUser.getId(),
				REFRESH_TOKEN_EXPIRATION_TIME);

		HashMap<String, String> response = new HashMap<String, String>();
		response.put("accessToken", accessToken);

		ResponseCookie responseCookies = Utils.generateResponseCookie("refreshToken", refreshToken,
				REFRESH_TOKEN_EXPIRATION_TIME);

		ApiResponse<HashMap<String, String>> apiRes = new ApiResponse<HashMap<String, String>>(
				"User created successfully", false, response);

		return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.SET_COOKIE, responseCookies.toString())
				.body(apiRes);
	}

	public ResponseEntity<ApiResponse<HashMap<String, String>>> loginUser(@Valid UserRegister user) {
		User dbUser = user_repo.findByUsername(user.getUsername())
				.orElseThrow(() -> new RuntimeException("Invalid User or Credentials"));

		if (!passEncoder.matches(user.getPassword(), dbUser.getPassword())) {
			throw new RuntimeException("Invalid User or Credentials");
		}
		String accessToken = jwtUtils.generateToken(user.getUsername(), dbUser.getId(), ACCESS_TOKEN_EXPIRATION_TIME);
		String refreshToken = jwtUtils.generateToken(user.getUsername(), dbUser.getId(), REFRESH_TOKEN_EXPIRATION_TIME);

		HashMap<String, String> response = new HashMap<String, String>();
		response.put("accessToken", accessToken);

		ResponseCookie responseCookies = Utils.generateResponseCookie("refreshToken", refreshToken,
				REFRESH_TOKEN_EXPIRATION_TIME);

		ApiResponse<HashMap<String, String>> apiRes = new ApiResponse<HashMap<String, String>>(
				"User logged in successfully", false, response);

		return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, responseCookies.toString())
				.body(apiRes);
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

	public ResponseEntity<ApiResponse<HashMap<String, String>>> refresh(String refreshCookie) {

		System.out.println(refreshCookie);
		HashMap<String, String> response = new HashMap<String, String>();
		ApiResponse<HashMap<String, String>> apiRes = new ApiResponse<HashMap<String, String>>(null, false, response);

//		if no cookie return 401

		if (refreshCookie == null) {
			apiRes.setMessage("Invalid token");
			apiRes.setSuccess(false);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
		}
		try {

//	 if Invalid or expired refresh token	

			String username = jwtUtils.extractUsername(refreshCookie);
			System.out.println("username"+username);
			if (!jwtUtils.isTokenValid(refreshCookie, username)) {
				apiRes.setMessage("Invalid or expired refresh token");
				apiRes.setSuccess(false);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
			}

//	Check if user still exists or not
			Optional<User> existingUser = user_repo.findByUsername(username);

			if (existingUser.isEmpty()) {
				apiRes.setMessage("User doesn't exists");
				apiRes.setSuccess(false);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
			}

//	Generate new access token

			String accessToken = jwtUtils.generateToken(existingUser.get().getUsername(), existingUser.get().getId(),
					ACCESS_TOKEN_EXPIRATION_TIME);
			response.put("accessToken", accessToken);

			apiRes.setMessage("Token refreshed successfully");
			apiRes.setSuccess(true);
			return ResponseEntity.status(HttpStatus.CREATED).body(apiRes);
		} catch (Exception e) {
			apiRes.setMessage("Invalid refresh token");
			apiRes.setSuccess(false);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
		}
	}
}
