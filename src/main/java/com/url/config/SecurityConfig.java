package com.url.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.url.filters.JwtFilter;
import com.url.models.MyUserDetailsService;


import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

	private JwtFilter jwtfilter;
	private MyUserDetailsService myuserdetailsservice;

	public SecurityConfig(JwtFilter jwtfilter, MyUserDetailsService myuserdetailsservice) {
		this.jwtfilter = jwtfilter;
		this.myuserdetailsservice = myuserdetailsservice;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf((csrf) -> csrf.disable()).cors(withDefaults())
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/api/v1/users/**", "/actuator/**", "/api/v1/url/r/*").permitAll()
								.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtfilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	AuthenticationManager authManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
		auth.userDetailsService(myuserdetailsservice).passwordEncoder(passEncoder());

		return auth.build();
	}

	@Bean
	PasswordEncoder passEncoder() {
		return new BCryptPasswordEncoder();

	}

	@Value("${frontend.origins}")
	String allowedOrigins;

	@Value("${frontend.methods}")
	String allowedMethods;

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		
		System.out.println(Arrays.asList(allowedOrigins.split(",")));
		System.out.println(Arrays.asList(allowedMethods.split(",")));
//		configuration.setAllowedOrigins(List.of("http://127.0.0.1:5173")); // your frontend domain
		configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(","))); // your frontend domain
		configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);// Used when sending cookies

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
