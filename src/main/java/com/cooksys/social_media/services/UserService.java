package com.cooksys.social_media.services;

import java.util.List;

import com.cooksys.social_media.dtos.CredentialsDto;
import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.dtos.UserRequestDto;
import com.cooksys.social_media.dtos.UserResponseDto;

public interface UserService {

	List<UserResponseDto> getAllUsers();

	UserResponseDto createUser(UserRequestDto userRequestDto);

	UserResponseDto getUserByUsername(String name);

	UserResponseDto deleteUser(String name, CredentialsDto credentials);

	void followUser(String usernameToFollow, CredentialsDto credentials);

	void unfollowUser(String usernameToFollow, CredentialsDto credentials);

	UserResponseDto updateByUsername(String username, UserRequestDto userRequestDto);

	List<TweetResponseDto> getTweetsByUsername(String username);

	List<UserResponseDto> getFollowers(String username);

	List<UserResponseDto> getFollowing(String username);

	List<TweetResponseDto> getMentions(String username);

	List<TweetResponseDto> getFeedByUsername(String username);

}
