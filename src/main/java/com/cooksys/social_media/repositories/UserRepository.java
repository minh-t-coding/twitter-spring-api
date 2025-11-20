package com.cooksys.social_media.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooksys.social_media.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	List<User> findAllByDeletedFalse();

	Optional<User> findByCredentialsUsernameAndDeletedFalse(String username);

	boolean existsByCredentialsUsername(String username);

	Optional<User> findByCredentialsUsername(String username);
}
