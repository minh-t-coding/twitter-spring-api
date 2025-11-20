package com.cooksys.social_media.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.entities.Tweet;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface TweetMapper {

	TweetResponseDto entityToDto(Tweet tweet);
	
	List<TweetResponseDto> entitiesToDtos(List<Tweet> tweetEntities);
}
