package com.codegym.socialmedia.config;

import com.codegym.socialmedia.service.user.UserActivityService;
import com.codegym.socialmedia.service.user.UserService;
import com.codegym.socialmedia.model.account.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

@Component
public class WebSocketActivityListener {

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserService userService;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        try {
            Principal principal = event.getUser();
            if (principal != null) {
                User user = getUserFromPrincipal(principal);
                if (user != null) {
                    userActivityService.updateActivity(user.getId());
                    System.out.println("✅ User " + user.getUsername() + " connected and marked as online");
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling connect event: " + e.getMessage());
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        try {
            Principal principal = event.getUser();
            if (principal != null) {
                User user = getUserFromPrincipal(principal);
                if (user != null) {
                    userActivityService.setUserOffline(user.getId());
                    System.out.println("❌ User " + user.getUsername() + " disconnected and marked as offline");
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling disconnect event: " + e.getMessage());
        }
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        try {
            Principal principal = event.getUser();
            if (principal != null) {
                User user = getUserFromPrincipal(principal);
                if (user != null) {
                    userActivityService.updateActivity(user.getId());
                    // Không log để tránh spam
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling subscribe event: " + e.getMessage());
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        try {
            if (principal instanceof Authentication) {
                Authentication auth = (Authentication) principal;
                Object principalObj = auth.getPrincipal();

                if (principalObj instanceof UserDetails) {
                    String username = ((UserDetails) principalObj).getUsername();
                    return userService.getUserByUsername(username);
                } else if (principalObj instanceof OAuth2User) {
                    OAuth2User oauth2User = (OAuth2User) principalObj;
                    String email = oauth2User.getAttribute("email");
                    return userService.findByEmail(email);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting user from principal: " + e.getMessage());
        }
        return null;
    }
}