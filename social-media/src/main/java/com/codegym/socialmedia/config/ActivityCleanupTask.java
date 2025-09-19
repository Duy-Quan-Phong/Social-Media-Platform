package com.codegym.socialmedia.config;

import com.codegym.socialmedia.service.user.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ActivityCleanupTask {

    @Autowired
    private UserActivityService userActivityService;

    // Cleanup mỗi 1 giờ
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000ms
    public void cleanupOldActivity() {
        try {
            userActivityService.cleanup();
            System.out.println("🧹 Cleaned up old activity data");
        } catch (Exception e) {
            System.err.println("❌ Error during activity cleanup: " + e.getMessage());
        }
    }

    // Log số user online mỗi 10 phút (để debug)
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void logOnlineStats() {
        try {
            int onlineCount = userActivityService.getOnlineUsers().size();
            System.out.println("📊 Online users: " + onlineCount);
        } catch (Exception e) {
            System.err.println("❌ Error logging online stats: " + e.getMessage());
        }
    }
}