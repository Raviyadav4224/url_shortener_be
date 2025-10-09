package com.url.models;

import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 50, unique = true)
	@NotBlank(message = "Username is required")
	private String username;

	@Column(nullable = false, length = 100)
	@NotBlank(message = "Password is required")
	private String password;

	private String Role = "ROLE_USER";

	@OneToMany(mappedBy = "createdBy", orphanRemoval = true, cascade = CascadeType.ALL)
	private List<UrlMapping> urlMappings = new ArrayList<UrlMapping>();
}
