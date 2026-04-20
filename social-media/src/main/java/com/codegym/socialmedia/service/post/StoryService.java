package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.component.CloudinaryService;
import com.codegym.socialmedia.dto.StoryDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.Story;
import com.codegym.socialmedia.repository.StoryRepository;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoryService {

    private final StoryRepository storyRepository;
    private final CloudinaryService cloudinaryService;
    private final FriendshipService friendshipService;

    public Story createStory(User user, MultipartFile media, String caption) {
        String mediaUrl = cloudinaryService.upload(media);
        Story.MediaType mediaType = media.getContentType() != null && media.getContentType().startsWith("video")
                ? Story.MediaType.VIDEO : Story.MediaType.IMAGE;

        Story story = new Story();
        story.setUser(user);
        story.setMediaUrl(mediaUrl);
        story.setMediaType(mediaType);
        story.setCaption(caption);
        story.setExpiresAt(LocalDateTime.now().plusHours(24));
        return storyRepository.save(story);
    }

    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesFeed(User currentUser) {
        // Get friends
        List<User> friends = friendshipService.findAllFriendshipsOfUser(currentUser.getId())
                .stream()
                .map(f -> f.getRequester().getId().equals(currentUser.getId()) ? f.getAddressee() : f.getRequester())
                .filter(u -> friendshipService.getFriendshipStatus(currentUser, u) == Friendship.FriendshipStatus.ACCEPTED)
                .collect(Collectors.toList());

        // Include current user's own stories
        friends.add(currentUser);

        List<Story> stories = storyRepository.findActiveStoriesFromFriends(friends, LocalDateTime.now());

        // Group by user
        Map<Long, StoryDto> grouped = new LinkedHashMap<>();
        for (Story s : stories) {
            grouped.computeIfAbsent(s.getUser().getId(), id -> new StoryDto(s.getUser()))
                   .addStory(s);
        }
        return new ArrayList<>(grouped.values());
    }

    public void viewStory(Long storyId) {
        storyRepository.findById(storyId).ifPresent(s -> {
            s.setViewCount(s.getViewCount() + 1);
            storyRepository.save(s);
        });
    }

    public boolean deleteStory(Long storyId, User user) {
        return storyRepository.findById(storyId).map(s -> {
            if (!s.getUser().getId().equals(user.getId())) return false;
            storyRepository.delete(s);
            return true;
        }).orElse(false);
    }

    @Scheduled(fixedRate = 3600000) // every hour
    public void cleanExpiredStories() {
        storyRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned expired stories");
    }
}
