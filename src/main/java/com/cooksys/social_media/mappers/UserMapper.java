package com.cooksys.social_media.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cooksys.social_media.dtos.UserRequestDto;
import com.cooksys.social_media.dtos.UserResponseDto;
import com.cooksys.social_media.entities.User;

@Mapper(componentModel = "spring", uses = { CredentialMapper.class, ProfileMapper.class })
public interface UserMapper {

	@Mapping(target = "username", source = "credentials.username")
	UserResponseDto getOneUser(User user);
	
	List<UserResponseDto> allUsers(List<User> users);

	User requestDtoToEntity(UserRequestDto userRequestDto);
	
//	User fromDto(UserDto userdto);

	
}
