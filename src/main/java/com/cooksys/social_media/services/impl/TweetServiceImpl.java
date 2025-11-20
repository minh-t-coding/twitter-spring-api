package com.cooksys.social_media.services.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksys.social_media.dtos.ContextDto;
import com.cooksys.social_media.dtos.CredentialsDto;
import com.cooksys.social_media.dtos.TweetRequestDto;
import com.cooksys.social_media.dtos.TweetResponseDto;
import com.cooksys.social_media.dtos.UserResponseDto;
import com.cooksys.social_media.entities.HashTag;
import com.cooksys.social_media.entities.Tweet;
import com.cooksys.social_media.entities.User;
import com.cooksys.social_media.exceptions.BadRequestException;
import com.cooksys.social_media.exceptions.NotAuthorizedException;
import com.cooksys.social_media.exceptions.NotFoundException;
import com.cooksys.social_media.mappers.TweetMapper;
import com.cooksys.social_media.mappers.UserMapper;
import com.cooksys.social_media.repositories.HashTagRepository;
import com.cooksys.social_media.repositories.TweetRepository;
import com.cooksys.social_media.repositories.UserRepository;
import com.cooksys.social_media.services.TweetService;
import com.cooksys.social_media.dtos.HashTagResponseDto;
import com.cooksys.social_media.mappers.HashTagMapper;
import java.util.Collections;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TweetServiceImpl implements TweetService {

	private final TweetRepository tweetRepository;
	private final UserRepository userRepository;
	private final HashTagRepository hashtagRepository;

	private final TweetMapper tweetMapper;
	private final UserMapper userMapper;
	private final HashTagMapper hashTagMapper;

	@Override
	public List<TweetResponseDto> getAllTweets() {
		return tweetMapper.entitiesToDtos(tweetRepository.findAllByDeletedFalseOrderByPostedDesc());
	}

	@Override
	@Transactional
	public TweetResponseDto createTweet(TweetRequestDto request) {
		validateTweetRequestDto(request);
		User author = getUserFromCredentials(request.getCredentials());
		Tweet tweet = new Tweet();
		tweet.setAuthor(author);
		tweet.setContent(request.getContent());

		Set<HashTag> hashtags = processHashTags(request.getContent());
		tweet.setHashtags(hashtags);
		Set<User> mentionedUsers = processMentionedUsers(request.getContent());
		tweet.setMentions(mentionedUsers);

		// Add tweet to mentioned user's mentions
		for (User mentionedUser : mentionedUsers) {
			if (mentionedUser.getMentions() == null) {
				mentionedUser.setMentions(new HashSet<>());
			}
			mentionedUser.getMentions().add(tweet);
			userRepository.saveAndFlush(mentionedUser);
		}

		return tweetMapper.entityToDto(tweetRepository.saveAndFlush(tweet));
	}

	@Override
	public TweetResponseDto createReplyTweet(Long id, TweetRequestDto request) {
		validateTweetRequestDto(request);
		Tweet parentTweet = getValidTweetById(id);
		User author = getUserFromCredentials(request.getCredentials());
		Tweet tweet = new Tweet();
		tweet.setAuthor(author);
		tweet.setContent(request.getContent());
		tweet.setInReplyTo(parentTweet);

		Set<HashTag> hashtags = processHashTags(request.getContent());
		tweet.setHashtags(hashtags);
		Set<User> mentionedUsers = processMentionedUsers(request.getContent());
		tweet.setMentions(mentionedUsers);

		// Add tweet to mentioned user's mentions
		for (User mentionedUser : mentionedUsers) {
			if (mentionedUser.getMentions() == null) {
				mentionedUser.setMentions(new HashSet<>());
			}
			mentionedUser.getMentions().add(tweet);
			userRepository.saveAndFlush(mentionedUser);
		}

		// add tweet to parent's replies
		parentTweet.getReplies().add(tweet);
		tweetRepository.saveAndFlush(parentTweet);

		return tweetMapper.entityToDto(tweetRepository.saveAndFlush(tweet));
	}

	@Override
	public TweetResponseDto getTweet(Long id) {
		Tweet tweet = getValidTweetById(id);
		return tweetMapper.entityToDto(tweet);
	}

	@Override
	public List<TweetResponseDto> getAllTaggedTweets(String label) {
		Optional<HashTag> optionalHashtag = hashtagRepository.findByLabel(label);
		if (optionalHashtag.isEmpty()) {
			throw new NotFoundException("No hashtags with label " + label + " found");
		}
		HashTag tag = optionalHashtag.get();
		List<Tweet> taggedTweets = tweetRepository.findAllByHashtagsContainingAndDeletedFalseOrderByPostedDesc(tag);
		return tweetMapper.entitiesToDtos(taggedTweets);
	}

	@Override
	public List<UserResponseDto> getUsersMentioned(Long tweetId) {
		Tweet tweet = getValidTweetById(tweetId);
		List<User> mentionedUsers = new ArrayList<>();
		for (User user : tweet.getMentions()) {
			if (!user.isDeleted()) {
				mentionedUsers.add(user);
			}
		}

		return userMapper.allUsers(mentionedUsers);
	}

	@Override
	public List<TweetResponseDto> getTweetReposts(Long tweetId) {
		Tweet tweet = getValidTweetById(tweetId);
		List<Tweet> reposts = new ArrayList<>();
		for (Tweet repost : tweet.getReposts()) {
			if (!repost.isDeleted()) {
				reposts.add(repost);
			}
		}

		return tweetMapper.entitiesToDtos(reposts);
	}

	@Override
	public List<TweetResponseDto> getTweetReplies(Long tweetId) {
		Tweet tweet = getValidTweetById(tweetId);
		List<Tweet> replies = new ArrayList<>();
		for (Tweet reply : tweet.getReplies()) {
			if (!reply.isDeleted()) {
				replies.add(reply);
			}
		}
		return tweetMapper.entitiesToDtos(replies);
	}

	@Override
	@Transactional
	public TweetResponseDto deleteTweet(Long id, CredentialsDto credentials) {
	    if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
	        throw new BadRequestException("Username and password are required");
	    }

	    Tweet tweet = tweetRepository.findById(id).orElseThrow(() -> new NotFoundException("Tweet not found"));
	    if (tweet.isDeleted()) throw new NotFoundException("Tweet not found");

	    User author = tweet.getAuthor();
	    if (author == null) throw new NotAuthorizedException("Invalid credentials");
	    if (author.getCredentials() == null
	            || !credentials.getUsername().equals(author.getCredentials().getUsername())
	            || !credentials.getPassword().equals(author.getCredentials().getPassword())) {
	        throw new NotAuthorizedException("Invalid credentials");
	    }

	    TweetResponseDto dto = tweetMapper.entityToDto(tweet);
	    tweet.setDeleted(true);
	    tweetRepository.saveAndFlush(tweet);
	    return dto;
	}


	@Override
	@Transactional
	public void likeTweet(Long id, CredentialsDto credentials) {
	    // Validate credentials DTO
	    if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
	        throw new BadRequestException("Username and password are required");
	    }

	    // Load tweet and ensure it exists and isn't deleted
	    Tweet tweet = tweetRepository.findById(id)
	            .orElseThrow(() -> new NotFoundException("Tweet not found"));
	    if (tweet.isDeleted()) throw new NotFoundException("Tweet not found");

	    // Load user and validate credentials
	    Optional<User> optUser = userRepository.findByCredentialsUsernameAndDeletedFalse(credentials.getUsername());
	    if (optUser.isEmpty()) throw new NotAuthorizedException("Invalid credentials");
	    User user = optUser.get();
	    if (!user.getCredentials().getPassword().equals(credentials.getPassword())) {
	        throw new NotAuthorizedException("Invalid credentials");
	    }

	    // Minimal-change: update only the tweet's likes set (safe if User has no likedTweets field)
	    if (tweet.getLikes() == null) tweet.setLikes(new HashSet<>());

	    if (!tweet.getLikes().contains(user)) {
	        tweet.getLikes().add(user);
	        tweetRepository.saveAndFlush(tweet);
	    }
	}



	@Override
	@Transactional
	public TweetResponseDto repostTweet(Long id, CredentialsDto credentials) {
	    // validate input
	    if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null) {
	        throw new BadRequestException("Username and password are required");
	    }

	    Tweet original = tweetRepository.findById(id).orElseThrow(() -> new NotFoundException("Tweet not found"));
	    if (original.isDeleted()) throw new NotFoundException("Tweet not found");

	    Optional<User> optUser = userRepository.findByCredentialsUsernameAndDeletedFalse(credentials.getUsername());
	    if (optUser.isEmpty()) throw new NotAuthorizedException("Invalid credentials");
	    User user = optUser.get();
	    if (!user.getCredentials().getPassword().equals(credentials.getPassword())) throw new NotAuthorizedException("Invalid credentials");

	    // create repost tweet
	    Tweet repost = new Tweet();
	    repost.setAuthor(user);
	    repost.setRepostOf(original);
	    repost.setContent(null);
	    repost.setDeleted(false);
	    repost.setPosted(new Timestamp(System.currentTimeMillis()));
	    repost = tweetRepository.saveAndFlush(repost);

	    // add repost to original's reposts set and save original
	    if (original.getReposts() == null) original.setReposts(new HashSet<>());
	    original.getReposts().add(repost);
	    tweetRepository.saveAndFlush(original);

	    return tweetMapper.entityToDto(repost);
	}



	@Override
	public List<HashTagResponseDto> getTweetTags(Long id) {
	    Tweet tweet = tweetRepository.findById(id).orElseThrow(() -> new NotFoundException("Tweet not found"));
	    if (tweet.isDeleted()) throw new NotFoundException("Tweet not found");
	    List<HashTagResponseDto> dtos = new ArrayList<>();
	    if (tweet.getHashtags() != null) {
	        for (HashTag ht : tweet.getHashtags()) {
	            dtos.add(hashTagMapper.entityToResponseDto(ht));
	        }
	    }
	    return dtos;
	}


	@Override
	public List<UserResponseDto> getTweetLikes(Long id) {
	    Tweet tweet = tweetRepository.findById(id).orElseThrow(() -> new NotFoundException("Tweet not found"));
	    if (tweet.isDeleted()) throw new NotFoundException("Tweet not found");
	    List<UserResponseDto> out = new ArrayList<>();
	    if (tweet.getLikes() != null) {
	        for (User u : tweet.getLikes()) {
	            if (!u.isDeleted()) {
	                out.add(userMapper.getOneUser(u));
	            }
	        }
	    }
	    return out;
	}


	@Override
	public ContextDto getTweetContext(Long id) {
	    Tweet target = tweetRepository.findById(id).orElseThrow(() -> new NotFoundException("Tweet not found"));
	    if (target.isDeleted()) throw new NotFoundException("Tweet not found");

	    // BEFORE: collect ancestors (oldest -> newest)
	    List<Tweet> beforeList = new ArrayList<>();
	    Tweet cur = target.getInReplyTo();
	    while (cur != null) {
	        if (!cur.isDeleted()) beforeList.add(cur);
	        cur = cur.getInReplyTo();
	    }
	    Collections.reverse(beforeList);

	    // AFTER: collect all descendant replies (flatten) in chronological order
	    List<Tweet> afterList = new ArrayList<>();
	    collectRepliesFlatten(target, afterList);
	    // sort afterList by posted timestamp
	    afterList.sort((a, b) -> a.getPosted().compareTo(b.getPosted()));

	    ContextDto ctx = new ContextDto();
	    ctx.setTarget(tweetMapper.entityToDto(target));
	    List<TweetResponseDto> beforeDtos = new ArrayList<>();
	    for (Tweet t : beforeList) beforeDtos.add(tweetMapper.entityToDto(t));
	    List<TweetResponseDto> afterDtos = new ArrayList<>();
	    for (Tweet t : afterList) if (!t.isDeleted()) afterDtos.add(tweetMapper.entityToDto(t));

	    ctx.setBefore(beforeDtos);
	    ctx.setAfter(afterDtos);
	    return ctx;
	}

	// helper
	private void collectRepliesFlatten(Tweet parent, List<Tweet> out) {
	    if (parent.getReplies() == null) return;
	    for (Tweet r : parent.getReplies()) {
	        // if reply is deleted, skip adding it but still traverse its children
	        if (!r.isDeleted()) out.add(r);
	        collectRepliesFlatten(r, out);
	    }
	}


	private void validateTweetRequestDto(TweetRequestDto tweetRequest) {
		if (tweetRequest.getContent() == null || tweetRequest.getCredentials() == null) {
			throw new BadRequestException("All fields are required on a tweet request dto");
		}
	}

	// Gives you a Tweet if it exists and is not deleted
	private Tweet getValidTweetById(Long tweetId) {
		Optional<Tweet> optionalTweet = tweetRepository.findById(tweetId);
		Tweet tweet;
		if (optionalTweet.isEmpty()) {
			throw new NotFoundException("Tweet with id : " + tweetId + " not found");
		} else {
			tweet = optionalTweet.get();
			if (tweet.isDeleted()) {
				throw new BadRequestException("Tweet with id " + tweetId + " has been deleted");
			}
		}
		return tweet;
	}

	private User getUserFromCredentials(CredentialsDto credentialsDto) {
		User user;
		if (credentialsDto.getPassword() == null || credentialsDto.getUsername() == null) {
			throw new BadRequestException("Username and password are required");
		}
		String username = credentialsDto.getUsername();
		Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (optionalUser.isEmpty()) {
			throw new NotFoundException("No active users found with username: " + username);
		} else {
			user = optionalUser.get();
			if (!user.getCredentials().getPassword().equals(credentialsDto.getPassword())) {
				throw new NotAuthorizedException("Invalid credentials for user: " + username);
			}
		}
		return user;
	}

	private Set<User> processMentionedUsers(String content) {
		Set<User> mentionedUsers = new HashSet<>();
		Pattern pattern = Pattern.compile("@(\\w+)");
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			String username = matcher.group(1);
			Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
			if (optionalUser.isPresent()) {
				mentionedUsers.add(optionalUser.get());
			}
		}
		return mentionedUsers;
	}

	private Set<HashTag> processHashTags(String content) {
		Set<HashTag> hashtags = new HashSet<>();
		Pattern pattern = Pattern.compile("#(\\w+)");
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			String label = matcher.group(1);
			Optional<HashTag> optionalHashtag = hashtagRepository.findByLabel(label);
			HashTag tag;
			if (optionalHashtag.isEmpty()) {
				tag = new HashTag();
				tag.setLabel(label);
			} else {
				tag = optionalHashtag.get();
			}
			tag.setLastUsed(Timestamp.from(Instant.now()));
			hashtagRepository.save(tag);
			hashtags.add(tag);
		}
		return hashtags;
	}

}
