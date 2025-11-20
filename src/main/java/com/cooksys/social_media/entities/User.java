package com.cooksys.social_media.entities;

import java.sql.Timestamp;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_table")
public class User {

	@Id
	@GeneratedValue
	private Long id;

//	@Embedded
	private Credential credentials;


	@CreationTimestamp
	private Timestamp joined;

	private boolean deleted = false;

	@Embedded
	private Profile profile;

	@OneToMany(mappedBy = "author")
	@SQLRestriction("deleted = false")
	private Set<Tweet> tweets;

	@ManyToMany(mappedBy = "likes")
	@SQLRestriction("deleted = false")
	private Set<Tweet> likedTweets;

	@ManyToMany(mappedBy = "mentions")
	@SQLRestriction("deleted = false")
	private Set<Tweet> mentions;

	@ManyToMany
	@JoinTable(
			name = "followers_following", 
			joinColumns = @JoinColumn(name = "following_id"), 
			inverseJoinColumns = @JoinColumn(name = "follower_id")
			)
	private Set<User> followers;

	@ManyToMany(mappedBy = "followers")
	private Set<User> following;
	
	
}
