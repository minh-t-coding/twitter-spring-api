package com.cooksys.social_media.services;

public interface ValidateService {
	
	boolean usernameExists(String username);

	boolean tagExists(String label);

	boolean usernameAvailable(String username);

}
