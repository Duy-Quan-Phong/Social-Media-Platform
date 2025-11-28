package socialmedia.controller;

import socialmedia.dto.post.PostCreateDto;
import socialmedia.model.PrivacyLevel;
import socialmedia.model.account.User;
import socialmedia.service.post.PostService;
import socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class CommonController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @GetMapping("/news-feed")
    public String postsPage(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("postCreateDto", new PostCreateDto());
        model.addAttribute("privacyLevels", PrivacyLevel.values());

        return "news-feed";
    }

}
