package com.url.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.url.dto.ApiResponse;
import com.url.dto.UrlRequest;
import com.url.models.UrlMapping;
import com.url.models.User;
import com.url.repository.UrlRepo;
import com.url.repository.UserRepo;

@Service
public class UrlMappingService {

	private UrlRepo url_repo;
	private UserRepo user_repo;
	private RedisTemplate<String, Object> redisTemplate;

	public UrlMappingService(UrlRepo url_repo, UserRepo user_repo, RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.url_repo = url_repo;
		this.user_repo = user_repo;
	}

	@Value("${base.app.url}")
	public String BASE_APP_URL;

	public UrlMapping shortenUrl(UrlRequest request, User user) {

		String shortCode = generateShortCode(request.getOriginalUrl());

		UrlMapping response = new UrlMapping();

		response.setOriginalUrl(request.getOriginalUrl());
		response.setCreatedBy(user);
		response.setShortUrl(shortCode);

		response.setExpiresAt(LocalDateTime.now().plusMinutes(30));

		url_repo.save(response);

//		add in redis
		redisTemplate.opsForValue().set(shortCode, response,
				Duration.between(LocalDateTime.now(), response.getExpiresAt()));

		return response;
	}

	private String generateShortCode(String originalUrl) {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 7; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	public List<UrlMapping> getAllUrls(Integer userId) {

		User user = user_repo.findById(userId).orElseThrow(() -> new RuntimeException("No URL mapping's found"));

		List<UrlMapping> result = user.getUrlMappings().stream().map(url -> {
			url.setShortUrl(BASE_APP_URL + "/r/" + url.getShortUrl());
			return url;
		}).toList();
		return result;
	}

	public ResponseEntity<ApiResponse<String>> removeUrl(Integer urlId, User user) {
		ApiResponse<String> response = new ApiResponse<String>(null, false, null);
		if (!user.getUrlMappings().isEmpty()) {
			user.getUrlMappings().removeIf(url -> url.getId().equals(urlId));
			user_repo.save(user);
			response.setMessage("success");
			response.setData("Url removed successfully");
		} else {
			response.setMessage("Invalid");
			response.setSuccess(false);
			response.setData("No mapped ID found");
		}

		return new ResponseEntity<ApiResponse<String>>(response, HttpStatus.OK);
	}

	public String getOriginalUrl(String shortCode) {

//		Check in REDIS
		Object cacheUrl = redisTemplate.opsForValue().get(shortCode);
		if (cacheUrl != null && cacheUrl instanceof UrlMapping) {

			UrlMapping cachedUrlMapping = (UrlMapping) cacheUrl;
			if (cachedUrlMapping.getExpiresAt().isBefore(LocalDateTime.now())) {
				redisTemplate.delete(shortCode);
				throw new RuntimeException("URL has expired");
			}
			cachedUrlMapping.setClickCount(cachedUrlMapping.getClickCount() + 1);
			redisTemplate.opsForValue().set(shortCode, cachedUrlMapping,
					Duration.between(LocalDateTime.now(), cachedUrlMapping.getExpiresAt()));

			return cachedUrlMapping.getOriginalUrl();

		}

//		GET FROM DB
		UrlMapping mapping = url_repo.findByShortUrl(shortCode)
				.orElseThrow(() -> new RuntimeException("URL Not Found"));

		if (mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("URL Expired");
		}

//		increase click cpunt
		mapping.setClickCount(mapping.getClickCount() + 1);

//		save in DB
		url_repo.save(mapping);
//		save in Redis
		redisTemplate.opsForValue().set(shortCode, mapping,
				Duration.between(LocalDateTime.now(), mapping.getExpiresAt()));
		return BASE_APP_URL + mapping.getOriginalUrl();
	}

}
