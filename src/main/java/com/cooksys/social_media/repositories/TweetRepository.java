package com.cooksys.social_media.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooksys.social_media.entities.HashTag;
import com.cooksys.social_media.entities.Tweet;
import com.cooksys.social_media.entities.User;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {
	Optional<Tweet> findById(Long tweetId);

	List<Tweet> findAllByDeletedFalseOrderByPostedDesc();

	List<Tweet> findAllByHashtagsContainingAndDeletedFalseOrderByPostedDesc(HashTag hashtag);

	List<Tweet> findAllByContentContainingAndDeletedFalseOrderByPostedDesc(String substring);

	List<Tweet> findAllByAuthorAndDeletedFalse(User author);
}