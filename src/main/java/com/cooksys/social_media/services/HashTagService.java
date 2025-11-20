package com.cooksys.social_media.services;

import java.util.List;

import com.cooksys.social_media.dtos.HashTagResponseDto;

public interface HashTagService {
    List<HashTagResponseDto> getAllTags();
}
