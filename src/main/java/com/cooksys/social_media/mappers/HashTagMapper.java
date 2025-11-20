package com.cooksys.social_media.mappers;

import org.mapstruct.Mapper;

import com.cooksys.social_media.dtos.HashTagResponseDto;
import com.cooksys.social_media.entities.HashTag;

@Mapper(componentModel = "spring")
public interface HashTagMapper {
	
	HashTag requestDtoToEntity(HashTagResponseDto hashTagResponseDto);
	
	HashTagResponseDto entityToResponseDto(HashTag hashtag);
}
