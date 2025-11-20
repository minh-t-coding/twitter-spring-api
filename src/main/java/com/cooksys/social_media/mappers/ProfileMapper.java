package com.cooksys.social_media.mappers;

import org.mapstruct.Mapper;

import com.cooksys.social_media.dtos.ProfileDto;
import com.cooksys.social_media.entities.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

	ProfileDto entityToDto(Profile entity);

	Profile dtoToEntity(ProfileDto dto);
}
