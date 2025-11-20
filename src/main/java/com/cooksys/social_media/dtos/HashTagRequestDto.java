package com.cooksys.social_media.dtos;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class HashTagRequestDto {
   
	private String label;
    private Timestamp firstUsed;
    private Timestamp lastUsed;
	

    
}
