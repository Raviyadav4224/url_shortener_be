package com.url.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.url.models.User;

public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
//			return Collections.emptyList(); // Creates Users with NO ROLE
		return List.of(new SimpleGrantedAuthority(user.getRole()));

	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername(); // Returning Email since we are storing user email in claims
	}

	public Integer getUserId() {
		return user.getId();
	}

}
