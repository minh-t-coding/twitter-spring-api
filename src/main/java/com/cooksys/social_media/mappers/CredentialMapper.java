package com.cooksys.social_media.mappers;

import org.mapstruct.Mapper;

import com.cooksys.social_media.dtos.CredentialsDto;
import com.cooksys.social_media.entities.Credential;

@Mapper(componentModel = "spring")
public interface CredentialMapper {

	CredentialsDto entityToDto(Credential Crendential);
	Credential dtoToEntity(CredentialsDto crendentialsDto);
}
