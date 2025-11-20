package com.cooksys.social_media.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksys.social_media.dtos.HashTagResponseDto;
import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.services.HashTagService;
import com.cooksys.social_media.services.TweetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class HashTagController {
	private final HashTagService hashtagService;
	private final TweetService tweetService;

	@GetMapping
	public List<HashTagResponseDto> getAllTags() {
		return hashtagService.getAllTags();
	}

	@GetMapping("/{label}")
	public List<TweetResponseDto> getTaggedTweets(@PathVariable String label) {
		return tweetService.getAllTaggedTweets(label);
	}
}
