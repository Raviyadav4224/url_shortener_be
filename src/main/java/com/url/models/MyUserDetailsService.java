package com.url.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.url.config.CustomUserDetails;
import com.url.repository.UserRepo;

@Service
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepo user_repo;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		User user = user_repo.findByUsername(email).orElseThrow(() -> new UsernameNotFoundException("User not Found"));

//			return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
//					new ArrayList<>());

		CustomUserDetails myUser = new CustomUserDetails(user);
		return myUser;
	}

}
