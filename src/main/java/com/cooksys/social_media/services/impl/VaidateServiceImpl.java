package com.cooksys.social_media.services.impl;

import org.springframework.stereotype.Service;

import com.cooksys.social_media.repositories.HashTagRepository;
import com.cooksys.social_media.repositories.UserRepository;
import com.cooksys.social_media.services.ValidateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VaidateServiceImpl implements ValidateService{
	
	
	private final UserRepository userRepository;
    private final HashTagRepository hashTagRepository;
	
	@Override
	public boolean usernameExists(String username) {
		return userRepository.findByCredentialsUsernameAndDeletedFalse(username).isPresent();
	}

	@Override
	public boolean tagExists(String label) {
		return hashTagRepository.findByLabel(label).isPresent();
	}

	@Override
	public boolean usernameAvailable(String username) {
		return !userRepository.findByCredentialsUsername(username).isPresent();
	}
	
	
}
