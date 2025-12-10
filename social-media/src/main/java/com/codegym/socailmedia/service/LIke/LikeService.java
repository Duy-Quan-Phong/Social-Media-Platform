package com.codegym.socailmedia.service.LIke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.codegym.socailmedia.model.Hashtag;
import com.codegym.socailmedia.model.account.User;
import com.codegym.socailmedia.model.social_action.LikePost;
import com.codegym.socailmedia.model.social_action.LikePostId;
import com.codegym.socailmedia.model.social_action.Post;
import com.codegym.socailmedia.repository.Hashtag.PostHashtagRepository;
import com.codegym.socailmedia.model.UserHashtagInterest;
import com.codegym.socailmedia.repository.Hashtag.UserHashtagInterestRepository;
import com.codegym.socailmedia.repository.UserRepository;
import com.codegym.socailmedia.repository.post.PostLikeRepository;
import com.codegym.socailmedia.repository.post.PostRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository; // Đảm bảo bạn có Repo này

    // --- CÁC REPO CHO AI (Cần tạo thêm) ---
    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private UserHashtagInterestRepository interestRepository;

    /**
     * Hàm xử lý Toggle Like (Thích / Bỏ thích)
     * Đồng thời cập nhật điểm sở thích cho AI
     */
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        // 1. Lấy thông tin Post và User
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Kiểm tra xem đã like chưa
        Optional<LikePost> existingLike = postLikeRepository.findByPostAndUser(post, user);
        boolean isLiked;

        if (existingLike.isPresent()) {
            // --- TRƯỜNG HỢP UNLIKE (Bỏ thích) ---
            postLikeRepository.delete(existingLike.get());
            isLiked = false; // Trạng thái hiện tại là chưa like
        } else {
            // --- TRƯỜNG HỢP LIKE (Thích) ---
            LikePost newLike = new LikePost();
            LikePostId id = new LikePostId(userId, postId);

            newLike.setId(id);
            newLike.setPost(post);
            newLike.setUser(user);

            postLikeRepository.save(newLike);
            isLiked = true; // Trạng thái hiện tại là đã like
        }

        // 3. [QUAN TRỌNG CHO AI] Cập nhật điểm sở thích
        // Nếu Like -> Cộng điểm (+5)
        // Nếu Unlike -> Trừ điểm (-5)
        updateUserInterests(postId, userId, isLiked);

        return isLiked; // Trả về trạng thái để Controller biết mà đổi màu nút
    }

    // --- LOGIC PHỤ TRỢ CHO THUẬT TOÁN ---

    private void updateUserInterests(Long postId, Long userId, boolean isLiked) {
        try {
            // Lấy danh sách hashtag của bài viết này
            List<Hashtag> hashtags = postHashtagRepository.findHashtagsByPostId(postId);

            if (hashtags.isEmpty()) return; // Bài viết ko có hashtag thì thôi

            double scoreDelta = isLiked ? 5.0 : -5.0;

            for (Hashtag tag : hashtags) {
                // Tìm xem user đã có điểm sở thích với tag này chưa
                UserHashtagInterest interest = interestRepository.findByUserIdAndHashtagId(userId, tag.getId())
                        .orElse(new UserHashtagInterest(userId, tag.getId(), 0.0));

                // Tính điểm mới
                double newScore = interest.getInterestScore() + scoreDelta;
                if (newScore < 0) newScore = 0; // Không để âm

                interest.setInterestScore(newScore);
                interest.setLastInteraction(LocalDateTime.now());

                interestRepository.save(interest);
            }
        } catch (Exception e) {
            // Log lỗi nhưng không chặn luồng Like chính
            System.err.println("Lỗi cập nhật điểm AI: " + e.getMessage());
        }
    }
}