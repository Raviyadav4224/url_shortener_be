package com.url.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegister {
	private final static transient String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$";

	private String username;
	
	@Pattern(regexp = passwordRegex, message = "Password does not meets the criteria of having at least one digit, one lowercase letter, one uppercase letter, one special character, and a length between 8 and 20 characters")
	private String password;
}
