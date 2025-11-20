package com.cooksys.social_media.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cooksys.social_media.dtos.CredentialsDto;
import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.dtos.UserRequestDto;
import com.cooksys.social_media.dtos.UserResponseDto;
import com.cooksys.social_media.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
	private static final UserResponseDto UserService = null;
	private final UserService userService;

	@GetMapping
	public List<UserResponseDto> getAllUsers() {
		return userService.getAllUsers();
	}

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponseDto createUser(@RequestBody UserRequestDto userRequestDto) {
		return userService.createUser(userRequestDto);
	}

	@GetMapping("/@{username}")
	public UserResponseDto getUserbyUsername(@PathVariable String username) {
		return userService.getUserByUsername(username);
	}

	@DeleteMapping("/@{username}")
	public UserResponseDto deleteByUsername(@PathVariable String username, @RequestBody CredentialsDto credentials) {
		return userService.deleteUser(username, credentials);
	}

	@PostMapping("/@{username}/follow")
	public void followUser(@PathVariable String username, @RequestBody CredentialsDto credentials) {
		userService.followUser(username, credentials);
	}

	@PostMapping("/@{username}/unfollow")
	public void unfollowUser(@PathVariable String username, @RequestBody CredentialsDto credentials) {
		userService.unfollowUser(username, credentials);
	}

	@PatchMapping("/@{username}")
	public UserResponseDto updateByUsername(@PathVariable String username, @RequestBody UserRequestDto userRequestDto) {
		return userService.updateByUsername(username, userRequestDto);
	}

	@GetMapping("/@{username}/tweets")
	public List<TweetResponseDto> getTweetsByUsername(@PathVariable String username) {
		return userService.getTweetsByUsername(username);
	}

	@GetMapping("/@{username}/followers")
	public List<UserResponseDto> getFollowers(@PathVariable String username) {
		return userService.getFollowers(username);
	}

	@GetMapping("/@{username}/following")
	public List<UserResponseDto> getFollowing(@PathVariable String username) {
		return userService.getFollowing(username);
	}

	@GetMapping("/@{username}/mentions")
	public List<TweetResponseDto> getUserMentions(@PathVariable String username) {
		return userService.getMentions(username);
	}

	@GetMapping("/@{username}/feed")
	public List<TweetResponseDto> getFeedByUsername(@PathVariable String username) {
		return userService.getFeedByUsername(username);
	}

}
