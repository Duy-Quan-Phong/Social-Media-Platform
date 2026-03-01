package com.codegym.socialmedia.service.IReelService;

import com.codegym.socialmedia.model.Reel.Reel;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.ReelComment;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface IReelService {
    Reel save(Reel reel);
    Optional<Reel> findById(Long id);
    void deleteById(Long id);
    List<Reel> findAll();

    //Logic tải lên video
    Reel uploadReel(User user, MultipartFile videoFile,String caption);

    //logic đề xuất video
    List<Reel> getRecommendedReels(User currentUser);

    //logic tăng view
    void incrementViews(Long reelId);


    void toggleLike(Long reelId, Long userId);

    ReelComment addComment(Long reelId, Long userId, String content);

    List<ReelComment> getCommentsByReelId(Long reelId);
}
