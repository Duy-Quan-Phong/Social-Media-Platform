package socialmedia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import socialmedia.dto.post.PostDisplayDto;
import socialmedia.model.account.User;
import socialmedia.repository.UserRepository;
import socialmedia.service.post.PostService;
import socialmedia.service.user.UserService;

import java.util.List;

@Controller
public class SearchController {

    @Autowired private PostService postService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "posts") String type, // type = 'posts' hoặc 'people'
                             Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        String keyword = (q != null) ? q.replace("#", "").trim() : "";

        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type); // Để active tab

        if (!keyword.isEmpty()) {
            // 1. TÌM BÀI VIẾT
            if ("posts".equals(type)) {
                // Gọi hàm tìm bài viết theo hashtag mà ta đã làm ở bước trước
                Page<PostDisplayDto> posts = postService.searchPostsByHashtag(keyword, currentUser, PageRequest.of(0, 20));
                model.addAttribute("posts", posts.getContent());
            }

            // 2. TÌM NGƯỜI DÙNG
            else if ("people".equals(type)) {
                // Tìm người hay nói về chủ đề này
                List<User> people = userRepository.findUsersByHashtagUsage(keyword);
                model.addAttribute("people", people);
            }
        }

        return "search/search"; // File giao diện tìm kiếm
    }
}