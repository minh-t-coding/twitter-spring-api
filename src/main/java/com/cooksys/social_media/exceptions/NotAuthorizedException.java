package com.cooksys.social_media.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class NotAuthorizedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6095083328577835493L;
	private String message;
}
