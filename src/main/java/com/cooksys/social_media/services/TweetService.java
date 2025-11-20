package com.cooksys.social_media.services;

import java.util.List;

import com.cooksys.social_media.dtos.ContextDto;
import com.cooksys.social_media.dtos.TweetRequestDto;
import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.dtos.UserResponseDto;
import com.cooksys.social_media.dtos.HashTagResponseDto;
import com.cooksys.social_media.dtos.CredentialsDto;

public interface TweetService {
	List<TweetResponseDto> getAllTweets();

	TweetResponseDto createTweet(TweetRequestDto request);

	TweetResponseDto createReplyTweet(Long id, TweetRequestDto request);

	TweetResponseDto getTweet(Long id);

	List<UserResponseDto> getTweetLikes(Long id);

	TweetResponseDto deleteTweet(Long id, CredentialsDto credentials);

	void likeTweet(Long id, CredentialsDto credentials);

	TweetResponseDto repostTweet(Long id, CredentialsDto credentials);

	List<HashTagResponseDto> getTweetTags(Long id);

	ContextDto getTweetContext(Long id);

	List<TweetResponseDto> getAllTaggedTweets(String label);

	List<TweetResponseDto> getTweetReposts(Long id);

	List<TweetResponseDto> getTweetReplies(Long id);
	
	List<UserResponseDto> getUsersMentioned(Long tweetId);
	
	

	// add other signatures if needed
}
