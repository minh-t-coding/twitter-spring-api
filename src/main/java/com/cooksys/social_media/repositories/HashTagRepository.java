package com.cooksys.social_media.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooksys.social_media.entities.HashTag;

@Repository
public interface HashTagRepository extends JpaRepository<HashTag, Long> {
	Optional<HashTag> findByLabel(String label);
	Optional<HashTag> findByLabelIgnoreCase(String label);
}
