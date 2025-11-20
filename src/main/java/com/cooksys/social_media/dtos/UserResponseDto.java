package com.cooksys.social_media.dtos;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserResponseDto {
	
	private Long id;
	private String username;

	private ProfileDto profile;

	private Timestamp joined;
}
