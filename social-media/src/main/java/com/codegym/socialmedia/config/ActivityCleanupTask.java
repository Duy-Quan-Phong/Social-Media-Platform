package com.codegym.socialmedia.config;

import com.codegym.socialmedia.service.user.UserActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ActivityCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(ActivityCleanupTask.class);

    @Autowired
    private UserActivityService userActivityService;

    // Cleanup mỗi 1 giờ
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000ms
    public void cleanupOldActivity() {
        try {
            userActivityService.cleanup();
            log.info("Cleaned up old activity data");
        } catch (Exception e) {
            log.error("Error during activity cleanup: " + e.getMessage());
        }
    }

    // Log số user online mỗi 10 phút (để debug)
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void logOnlineStats() {
        try {
            int onlineCount = userActivityService.getOnlineUsers().size();
            log.info("Online users: " + onlineCount);
        } catch (Exception e) {
            log.error("Error logging online stats: " + e.getMessage());
        }
    }
}