package com.codegym.socailmedia.component;

import com.codegym.socailmedia.service.user.UserActivityService;
import com.codegym.socailmedia.service.user.UserService;
import com.codegym.socailmedia.model.account.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ActivityInterceptor implements HandlerInterceptor {

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // Chỉ track activity cho user đã đăng nhập
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                userActivityService.updateActivity(currentUser.getId());
            }
        } catch (Exception e) {
            // Không throw exception để không ảnh hưởng request chính
            System.err.println("Error tracking user activity: " + e.getMessage());
        }

        return true;
    }
}