package com.cooksys.social_media.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cooksys.social_media.dtos.ContextDto;
import com.cooksys.social_media.dtos.CredentialsDto;
import com.cooksys.social_media.dtos.HashTagRequestDto;
import com.cooksys.social_media.dtos.TweetRequestDto;
import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.dtos.UserResponseDto;
import com.cooksys.social_media.dtos.HashTagResponseDto;
import com.cooksys.social_media.services.TweetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tweets")
public class TweetController {

	private final TweetService tweetService;

	@GetMapping
	public List<TweetResponseDto> getAllTweets() {
		return tweetService.getAllTweets();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TweetResponseDto createTweet(@RequestBody TweetRequestDto request) {
		return tweetService.createTweet(request);
	}

	@GetMapping("/{id}")
	public TweetResponseDto getTweet(@PathVariable Long id) {
		return tweetService.getTweet(id);
	}

	@GetMapping("/{id}/mentions")
	public List<UserResponseDto> getUsersMentioned(@PathVariable Long id) {
		return tweetService.getUsersMentioned(id);
	}

	@GetMapping("/{id}/reposts")
	public List<TweetResponseDto> getTweetReposts(@PathVariable Long id) {
		return tweetService.getTweetReposts(id);
	}

	@GetMapping("/{id}/replies")
	public List<TweetResponseDto> getTweetReplies(@PathVariable Long id) {
		return tweetService.getTweetReplies(id);
	}

	@PostMapping("/{id}/reply")
	@ResponseStatus(HttpStatus.CREATED)
	public TweetResponseDto createReplyTweet(@PathVariable Long id, @RequestBody TweetRequestDto request) {
		return tweetService.createReplyTweet(id, request);
	}

	@DeleteMapping("/{id}") // if your route is /tweets/{id}
	public ResponseEntity<TweetResponseDto> deleteTweet(@PathVariable Long id,
	        @RequestBody CredentialsDto credentials) {
	    TweetResponseDto dto = tweetService.deleteTweet(id, credentials);
	    return ResponseEntity.ok(dto);
	}

	@PostMapping("/{id}/like")
	public ResponseEntity<Void> likeTweet(@PathVariable Long id, @RequestBody CredentialsDto credentials) {
	    tweetService.likeTweet(id, credentials);
	    return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/repost")
	public ResponseEntity<TweetResponseDto> repostTweet(@PathVariable Long id,
	        @RequestBody CredentialsDto credentials) {
	    TweetResponseDto dto = tweetService.repostTweet(id, credentials);
	    return ResponseEntity.ok(dto);
	}

	@GetMapping("/{id}/tags")
	public ResponseEntity<List<HashTagResponseDto>> getTags(@PathVariable Long id) {
	    List<HashTagResponseDto> tags = tweetService.getTweetTags(id);
	    return ResponseEntity.ok(tags);
	}

	@GetMapping("/{id}/likes")
	public ResponseEntity<List<UserResponseDto>> getLikes(@PathVariable Long id) {
	    List<UserResponseDto> likes = tweetService.getTweetLikes(id);
	    return ResponseEntity.ok(likes);
	}

	@GetMapping("/{id}/context")
	public ResponseEntity<ContextDto> getContext(@PathVariable Long id) {
		ContextDto ctx = tweetService.getTweetContext(id);
		return ResponseEntity.ok(ctx);
	}
}
