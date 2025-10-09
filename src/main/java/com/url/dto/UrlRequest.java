package com.url.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlRequest {
	private static final String URL_REGEX = "^(https?://)?(www\\.)?[a-zA-Z0-9\\-]+(\\.[a-zA-Z]{2,})+(/.*)?$";
	private static final String URL_REGEX_MESSAGE = "❌ Invalid URL format."
			+ "👉 Please enter a valid URL, for example:" + ",www.google.com/" + ",http://google.com/"
			+ ",https://google.com/";

	@Pattern(regexp = URL_REGEX, message = URL_REGEX_MESSAGE)
	private String originalUrl;

	private Integer userId;
}
