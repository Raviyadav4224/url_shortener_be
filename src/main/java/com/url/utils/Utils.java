package com.url.utils;

import org.springframework.http.ResponseCookie;

public class Utils {

	public static ResponseCookie generateResponseCookie(String cookiename, String cookieValue, long expiration) {
		return ResponseCookie.from(cookiename, cookieValue)
				.httpOnly(true)
				.secure(false)
				.path("/api/v1/users/refresh")
				.maxAge(expiration / 1000)
				.sameSite("Lax")
				.build();
	}
}
