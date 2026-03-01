package com.codegym.socialmedia.service.IReelService;

import com.codegym.socialmedia.component.CloudinaryService;
import com.codegym.socialmedia.model.Reel.Reel;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.ReelLike;
import com.codegym.socialmedia.model.social_action.ReelComment; // ⭐ MỚI: Import thêm cái này
import com.codegym.socialmedia.repository.IReelRepository;
import com.codegym.socialmedia.repository.ReelLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.codegym.socialmedia.repository.ReelCommentRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReelServicelmpl implements IReelService {

    @Autowired
    private com.codegym.socialmedia.repository.ReelCommentRepository reelCommentRepository;

    @Autowired
    private IReelRepository reelRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ReelLikeRepository reelLikeRepository;

    @Override
    public Reel save(Reel reel) {
        return reelRepository.save(reel);
    }

    @Override
    public Optional<Reel> findById(Long id) {
        return reelRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        reelRepository.deleteById(id);
    }

    @Override
    public List<Reel> findAll() {
        return reelRepository.findAll();
    }

    @Override
    public Reel uploadReel(User user, MultipartFile videoFile, String caption) {
        // 1. Up video lên Cloudinary
        String videoUrl = cloudinaryService.uploadVideo(videoFile);

        // 2. Tự động tạo Thumbnail
        String thumbnailUrl = videoUrl.substring(0, videoUrl.lastIndexOf('.')) + ".jpg";

        // 3. Trích xuất Hashtag
        String hashtags = extractHashtags(caption);

        // 4. Tạo Entity Reel
        Reel reel = new Reel();
        reel.setUser(user);
        reel.setVideoUrl(videoUrl);
        reel.setThumbnailUrl(thumbnailUrl);
        reel.setCaption(caption);
        reel.setHashtags(hashtags);
        reel.setDurationSeconds(15);

        // 5. Lưu vào Database
        return reelRepository.save(reel);
    }

    private String extractHashtags(String caption) {
        if (caption == null) return "";
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(caption);
        StringBuilder hashtags = new StringBuilder();
        while (matcher.find()) {
            if (hashtags.length() > 0) {
                hashtags.append(",");
            }
            hashtags.append(matcher.group(1).toLowerCase());
        }
        return hashtags.toString();
    }

    @Override
    public List<Reel> getRecommendedReels(User currentUser) {
        // --- PHẦN 1: LOGIC TÌM KIẾM VIDEO ---
        List<Reel> recommendedReels = reelRepository.findRandomReels(20);

        if (currentUser != null) {
            String assumedHashtag = "fun";
            List<Reel> hashtagReels = reelRepository.findByHashtag(assumedHashtag);
            recommendedReels.addAll(hashtagReels);

            recommendedReels = recommendedReels.stream()
                    .distinct()
                    .limit(20)
                    .collect(Collectors.toList());
        }

        // --- PHẦN 2: LOGIC KIỂM TRA TRẠNG THÁI LIKE ---
        if (currentUser != null) {
            for (Reel reel : recommendedReels) {
                boolean hasLiked = reelLikeRepository.existsByReelIdAndUserId(reel.getId(), currentUser.getId());
                reel.setLiked(hasLiked);
            }
        }
        return recommendedReels;
    }

    @Override
    public void incrementViews(Long reelId) {
        reelRepository.findById(reelId).ifPresent(reel -> {
            reel.setViewsCount(reel.getViewsCount() + 1);
            reelRepository.save(reel);
        });
    }

    @Override
    public void toggleLike(Long reelId, Long userId) {
        Reel reel = reelRepository.findById(reelId).orElse(null);
        if (reel == null) return;

        ReelLike existingLike = reelLikeRepository.findByReelIdAndUserId(reelId, userId);

        if (existingLike != null) {
            // UNLIKE
            reelLikeRepository.delete(existingLike);
            reel.setLikesCount(Math.max(0, reel.getLikesCount() - 1));
            reel.setLiked(false);
        } else {
            // LIKE
            User user = new User();
            user.setId(userId);
            ReelLike newLike = new ReelLike(reel, user);
            reelLikeRepository.save(newLike);
            reel.setLikesCount(reel.getLikesCount() + 1);
            reel.setLiked(true);
        }
        reelRepository.save(reel);
    }

    // ========================================================
    // ⭐ PHẦN CODE COMMENT ĐÃ ĐƯỢC THÊM VÀO
    // ========================================================
    @Override
    public ReelComment addComment(Long reelId, Long userId, String content) {
        // 1. Tìm Reel
        Reel reel = reelRepository.findById(reelId).orElse(null);
        if (reel == null) return null;

        // 2. Tạo User ảo
        User user = new User();
        user.setId(userId);

        // 3. Tạo Comment mới
        ReelComment comment = new ReelComment();
        comment.setReel(reel);
        comment.setUser(user);
        comment.setContent(content);

        // 4. Lưu Comment vào DB
        ReelComment savedComment = reelCommentRepository.save(comment);

        // 5. Tăng số lượng comment của Reel lên 1
        reel.setCommentsCount(reel.getCommentsCount() + 1);
        reelRepository.save(reel);

        return savedComment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReelComment> getCommentsByReelId(Long reelId) {
        return reelCommentRepository.findByReelIdOrderByCommentedAtAsc(reelId);
    }
}