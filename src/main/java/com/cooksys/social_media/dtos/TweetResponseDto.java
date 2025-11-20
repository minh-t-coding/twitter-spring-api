package com.cooksys.social_media.dtos;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TweetResponseDto {
	private Long id;
	private UserResponseDto author; // create UserResponseDto if not present
	private Timestamp posted;
	private String content; // optional
	private TweetResponseDto inReplyTo; // recursive
	private TweetResponseDto repostOf; // recursive
}
