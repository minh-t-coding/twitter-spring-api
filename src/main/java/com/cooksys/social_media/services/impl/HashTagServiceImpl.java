package com.cooksys.social_media.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cooksys.social_media.dtos.HashTagResponseDto;
import com.cooksys.social_media.entities.HashTag;
import com.cooksys.social_media.mappers.HashTagMapper;
import com.cooksys.social_media.repositories.HashTagRepository;
import com.cooksys.social_media.services.HashTagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HashTagServiceImpl implements HashTagService {

    private final HashTagRepository hashTagRepository;
    private final HashTagMapper hashTagMapper;

    @Override
    public List<HashTagResponseDto> getAllTags() {
        List<HashTag> tags = hashTagRepository.findAll();
        List<HashTagResponseDto> dtos = new ArrayList<>();
        for (HashTag t : tags) {
            dtos.add(hashTagMapper.entityToResponseDto(t));
        }
        return dtos;
    }
}
