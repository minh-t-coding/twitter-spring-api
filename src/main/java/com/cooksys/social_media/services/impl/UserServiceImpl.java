package com.cooksys.social_media.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cooksys.social_media.dtos.CredentialsDto;
import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.dtos.UserRequestDto;
import com.cooksys.social_media.dtos.UserResponseDto;
import com.cooksys.social_media.entities.Tweet;
import com.cooksys.social_media.entities.User;
import com.cooksys.social_media.exceptions.BadRequestException;
import com.cooksys.social_media.exceptions.NotAuthorizedException;
import com.cooksys.social_media.exceptions.NotFoundException;
import com.cooksys.social_media.mappers.TweetMapper;
import com.cooksys.social_media.mappers.UserMapper;
import com.cooksys.social_media.repositories.TweetRepository;
import com.cooksys.social_media.repositories.UserRepository;
import com.cooksys.social_media.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final TweetMapper tweetMapper;

	private final UserRepository userRepository;
	private final TweetRepository tweetRepository;

	@Override
	public UserResponseDto createUser(UserRequestDto userRequestDto) {
		// Validate required fields
		System.out.println("yo");
		if (userRequestDto.getCredentials() == null || userRequestDto.getProfile() == null
				|| userRequestDto.getCredentials().getUsername() == null
				|| userRequestDto.getCredentials().getPassword() == null
				|| userRequestDto.getProfile().getEmail() == null) {
			throw new BadRequestException("Missing required fields: credentials or profile");
		}

		String username = userRequestDto.getCredentials().getUsername();

		// ️Check if user exists (deleted or not)
		Optional<User> existingUserOpt = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (existingUserOpt.isPresent()) {
			throw new BadRequestException("Username '" + username + "' is already taken");
		}

		// ️Check if a deleted user exists with same credentials
		List<User> allUsers = userRepository.findAll();
		User deletedUser = null;

		for (User u : allUsers) {
			if (u.isDeleted() && u.getCredentials().getUsername().equals(username)) {
				deletedUser = u;
				break;
			}
		}

		if (deletedUser != null) {
			// Reactivate user
			deletedUser.setDeleted(false);
			deletedUser.setProfile(userMapper.requestDtoToEntity(userRequestDto).getProfile());
			User savedUser = userRepository.save(deletedUser);
			return userMapper.getOneUser(savedUser);
		}

		// create a new user
		User newUser = userMapper.requestDtoToEntity(userRequestDto);
		User savedUser = userRepository.save(newUser);
		return userMapper.getOneUser(savedUser);
	}

	@Override
	public UserResponseDto getUserByUsername(String name) {
		Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(name);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			return userMapper.getOneUser(user);
		} else {
			throw new NotFoundException("User not found!");
		}
	}

	@Override
	public List<UserResponseDto> getAllUsers() {
		return userMapper.allUsers(userRepository.findAllByDeletedFalse());

	}

	@Override
	public UserResponseDto deleteUser(String name, CredentialsDto credential) {

		Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(name);
		if (!optionalUser.isPresent()) {
			throw new NotFoundException("User not found!");
		}
		User user = optionalUser.get();

		if (credential == null || credential.getPassword() == null) {
			throw new BadRequestException("Password required");
		}

		if (!user.getCredentials().getPassword().equals(credential.getPassword())) {
			throw new NotAuthorizedException("Invalid credentials");
		}

		// Attempt to create a response copy BEFORE deletion, but be defensive
		UserResponseDto copyBeforeDelete;
		try {
			copyBeforeDelete = userMapper.getOneUser(user);
		} catch (Exception e) {
			// fallback: build a minimal DTO manually to avoid 500s
			copyBeforeDelete = new UserResponseDto();
			try {
				// best-effort population if setters exist
				copyBeforeDelete
						.setUsername(user.getCredentials() != null ? user.getCredentials().getUsername() : null);
			} catch (Throwable ignored) {
			}
		}

		// soft delete the user
		user.setDeleted(true);
		userRepository.save(user);

		return copyBeforeDelete;
	}

	@Override
	public void followUser(String usernameToFollow, CredentialsDto credentials) {
		if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
			throw new BadRequestException("Credentials (username and password) are required");
		}

		// validate the follower (credentials provided)
		Optional<User> optionalFollower = userRepository
				.findByCredentialsUsernameAndDeletedFalse(credentials.getUsername());
		if (!optionalFollower.isPresent()) {
			throw new NotFoundException("Follower not found");
		}
		User follower = optionalFollower.get();

		if (!follower.getCredentials().getPassword().equals(credentials.getPassword())) {
			throw new NotAuthorizedException("Invalid credentials");
		}

		// find the user to follow
		Optional<User> optionalFollowee = userRepository.findByCredentialsUsernameAndDeletedFalse(usernameToFollow);
		if (!optionalFollowee.isPresent()) {
			throw new NotFoundException("User to follow does not exist!");
		}
		User followee = optionalFollowee.get();

		// cannot follow yourself
		if (follower.getId().equals(followee.getId())) {
			throw new BadRequestException("Cannot follow yourself");
		}

		// initialize the following set if null
		if (follower.getFollowing() == null) {
			follower.setFollowing(new HashSet<>());
		}

		// check if already following
		for (User u : follower.getFollowing()) {
			if (u.getId().equals(followee.getId())) {
				throw new BadRequestException("Already following this user!");
			}
		}

		follower.getFollowing().add(followee);

		// save follower in folloee's follows
		if (followee.getFollowers() == null) {
			followee.setFollowers(new HashSet<>());
		}
		followee.getFollowers().add(follower);
		userRepository.saveAndFlush(followee);
		userRepository.saveAndFlush(follower);
	}

	@Override
	public void unfollowUser(String usernameToUnfollow, CredentialsDto credentials) {
		if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
			throw new BadRequestException("Credentials (username and password) are required");
		}

		// find the follower (providing credentials)
		Optional<User> optionalFollower = userRepository
				.findByCredentialsUsernameAndDeletedFalse(credentials.getUsername());
		if (!optionalFollower.isPresent()) {
			throw new NotFoundException("Follower not found or deleted");
		}
		User follower = optionalFollower.get();

		// validate credentials
		if (!follower.getCredentials().getPassword().equals(credentials.getPassword())) {
			throw new NotAuthorizedException("Invalid credentials");
		}

		// find the user to unfollow
		Optional<User> optionalFollowee = userRepository.findByCredentialsUsernameAndDeletedFalse(usernameToUnfollow);
		if (!optionalFollowee.isPresent()) {
			throw new NotFoundException("User to unfollow does not exist!");
		}
		User followee = optionalFollowee.get();

		// check if follower is actually following followee
		if (follower.getFollowing() == null || !follower.getFollowing().contains(followee)) {
			throw new BadRequestException("You are not following this user!");
		}

		follower.getFollowing().remove(followee);
		followee.getFollowers().remove(follower);

		userRepository.saveAndFlush(followee);
		userRepository.saveAndFlush(follower);
	}

	@Override
	public UserResponseDto updateByUsername(String username, UserRequestDto userRequestDto) {
		// validate input
		if (userRequestDto == null || userRequestDto.getCredentials() == null
				|| userRequestDto.getCredentials().getUsername() == null
				|| userRequestDto.getCredentials().getPassword() == null || userRequestDto.getProfile() == null) {
			throw new BadRequestException("Missing required fields: credentials!");
		}

		CredentialsDto credentials = userRequestDto.getCredentials();

		// find the target user (must not be deleted)
		Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (!optionalUser.isPresent()) {
			throw new NotFoundException("User not found!");
		}

		User user = optionalUser.get();

		// validate credentials
		if (!user.getCredentials().getUsername().equals(credentials.getUsername())
				|| !user.getCredentials().getPassword().equals(credentials.getPassword())) {
			throw new NotAuthorizedException("Invalid credentials for user update");
		}

		// update only profile fields
		if (userRequestDto.getProfile() != null) {
			if (userRequestDto.getProfile().getFirstName() != null) {
				user.getProfile().setFirstName(userRequestDto.getProfile().getFirstName());
			}
			if (userRequestDto.getProfile().getLastName() != null) {
				user.getProfile().setLastName(userRequestDto.getProfile().getLastName());
			}
			if (userRequestDto.getProfile().getEmail() != null) {
				user.getProfile().setEmail(userRequestDto.getProfile().getEmail());
			}
			if (userRequestDto.getProfile().getPhone() != null) {
				user.getProfile().setPhone(userRequestDto.getProfile().getPhone());
			}
		}

		User updatedUser = userRepository.save(user);
		return userMapper.getOneUser(updatedUser);
	}

	@Override
	public List<TweetResponseDto> getTweetsByUsername(String username) {
		User user;
		Optional<User> optionalUser = userRepository.findByCredentialsUsername(username);
		if (optionalUser.isEmpty()) {
			throw new NotFoundException("No active users found with username: " + username);
		}
		user = optionalUser.get();

		if (user.isDeleted()) {
			throw new BadRequestException("User with username " + username + " is deleted");
		}

		List<Tweet> sortedTweets = new ArrayList<>(tweetRepository.findAllByAuthorAndDeletedFalse(user));
		sortedTweets.sort((t1, t2) -> t2.getPosted().compareTo(t1.getPosted()));

		return tweetMapper.entitiesToDtos(sortedTweets);
	}

	@Override
	public List<UserResponseDto> getFollowers(String username) {
		Optional<User> opt = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (opt.isEmpty()) {
			throw new NotFoundException("No active user found with username: " + username);
		}
		User user = opt.get();
		List<User> followers = new ArrayList<>();
		if (user.getFollowers() != null) {
			for (User u : user.getFollowers()) {
				if (!u.isDeleted())
					followers.add(u);
			}
		}
		return userMapper.allUsers(followers);
	}

	@Override
	public List<UserResponseDto> getFollowing(String username) {
		Optional<User> opt = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (opt.isEmpty()) {
			throw new NotFoundException("No active user found with username: " + username);
		}
		User user = opt.get();
		List<User> following = new ArrayList<>();
		if (user.getFollowing() != null) {
			for (User u : user.getFollowing()) {
				if (!u.isDeleted())
					following.add(u);
			}
		}
		return userMapper.allUsers(following);
	}

	@Override
	public List<TweetResponseDto> getMentions(String username) {
		Optional<User> opt = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (opt.isEmpty()) {
			throw new NotFoundException("No active user found with username: " + username);
		}
		String mentionToken = "@" + username;
		List<Tweet> tweets = tweetRepository.findAllByContentContainingAndDeletedFalseOrderByPostedDesc(mentionToken);
		List<Tweet> result = new ArrayList<>();
		for (Tweet t : tweets) {
			if (!t.isDeleted() && t.getContent() != null && t.getContent().contains(mentionToken)) {
				result.add(t);
			}
		}
		return tweetMapper.entitiesToDtos(result);
	}

	@Override
	public List<TweetResponseDto> getFeedByUsername(String username) {
		User user;
		Optional<User> optionalUser = userRepository.findByCredentialsUsername(username);
		if (optionalUser.isEmpty()) {
			throw new NotFoundException("No active users found with username: " + username);
		}
		user = optionalUser.get();

		if (user.isDeleted()) {
			throw new BadRequestException("User with username " + username + " is deleted");
		}

		List<Tweet> feed = new ArrayList<>();
		feed.addAll(tweetRepository.findAllByAuthorAndDeletedFalse(user));

		for (User followedUser : user.getFollowing()) {
			feed.addAll(tweetRepository.findAllByAuthorAndDeletedFalse(followedUser));
		}
		feed.sort((t1, t2) -> t2.getPosted().compareTo(t1.getPosted()));
		return tweetMapper.entitiesToDtos(feed);
	}

}
