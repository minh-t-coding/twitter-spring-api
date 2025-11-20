package com.cooksys.social_media.entities;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
public class HashTag {

	@Id
	@GeneratedValue
	private Long id;

	private String label;

	@CreationTimestamp
	private Timestamp firstUsed;

	private Timestamp lastUsed;

}
