package com.url.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.url.models.UrlMapping;

@Repository
public interface UrlRepo extends JpaRepository<UrlMapping, Integer> {

	Optional<UrlMapping> findByShortUrl(String shortCode);

}
