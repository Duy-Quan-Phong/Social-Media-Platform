package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.model.Reel.Reel;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.IReelService.IReelService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

// Thêm DTO để trả về JSON cho Frontend

@Controller
@RequestMapping("/reels")
public class ReelController {

    @Autowired
    private IReelService reelService;

    @Autowired
    private UserService userService;

    // ========================================
    //      SHOW FEED (GIAO DIỆN CHÍNH)
    // ========================================
    @GetMapping
    public String showReelFeed(Model model) {
        User currentUser = userService.getCurrentUser();
        List<Reel> recommendedReels = reelService.getRecommendedReels(currentUser);
        model.addAttribute("reels", recommendedReels);
        return "reels/Reel_feed";
    }

    // ========================================
    //      SHOW UPLOAD FORM
    // ========================================
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "reels/Reel_upload";
    }

    // ========================================
    //      UPLOAD REEL (XỬ LÝ TẢI LÊN)
    // ========================================
    @PostMapping("/upload")
    public String uploadReelUI(
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam(value = "caption", required = false) String caption,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập!");
            return "redirect:/reels/upload";
        }

        try {
            reelService.uploadReel(currentUser, videoFile, caption);
            redirectAttributes.addFlashAttribute("message", "Tải lên thành công!");
            return "redirect:/reels";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải video lên!");
            return "redirect:/reels/upload";
        }
    }

    // ========================================
    //      API LIKE (XỬ LÝ THẢ TIM)
    // ========================================
    @PostMapping("/api/{id}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLikeApi(@PathVariable("id") Long reelId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
        }

        try {
            reelService.toggleLike(reelId, currentUser.getId());
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server");
        }
    }



    @PostMapping("/api/{id}/comment")
    @ResponseBody
    public ResponseEntity<?> addCommentApi(
            @PathVariable("id") Long reelId,
            @RequestBody String content) { // Nhận nội dung JSON thô

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
        }

        try {
            // Lọc sạch dấu ngoặc kép thừa do JSON gửi lên (Ví dụ: "Nội dung" -> Nội dung)
            String cleanContent = content != null ? content.replaceAll("^\"|\"$", "").replace("\\n", "\n") : "";

            if (cleanContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nội dung không được để trống");
            }

            // Gọi Service để lưu vào Database
            reelService.addComment(reelId, currentUser.getId(), cleanContent);

            return ResponseEntity.ok("Bình luận thành công");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi Server: " + e.getMessage());
        }
    }


    // ========================================
    //      API KHÁC (FEED JSON, TĂNG VIEW)
    // ========================================
    @GetMapping("/api/feed")
    public ResponseEntity<List<Reel>> getReelFeed() {
        User currentUser = userService.getCurrentUser();
        List<Reel> recommendedReels = reelService.getRecommendedReels(currentUser);
        return new ResponseEntity<>(recommendedReels, HttpStatus.OK);
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Long id) {
        reelService.incrementViews(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}