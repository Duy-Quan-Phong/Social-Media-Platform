package com.codegym.socialmedia.service.user;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class UserActivityService {

    // Lưu trạng thái online trong memory
    private final Map<Long, LocalDateTime> lastActivity = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> lastSeen = new ConcurrentHashMap<>();

    // Cập nhật hoạt động của user (được gọi khi user có action)
    public void updateActivity(Long userId) {
        lastActivity.put(userId, LocalDateTime.now());
    }

    // Đánh dấu user offline (khi logout hoặc disconnect)
    public void setUserOffline(Long userId) {
        if (lastActivity.containsKey(userId)) {
            lastSeen.put(userId, lastActivity.get(userId));
            lastActivity.remove(userId);
        }
    }

    // Kiểm tra user có online không (active trong 5 phút gần nhất)
    public boolean isOnline(Long userId) {
        LocalDateTime last = lastActivity.get(userId);
        if (last == null) return false;

        return ChronoUnit.MINUTES.between(last, LocalDateTime.now()) <= 5;
    }

    // Lấy thời gian hoạt động cuối
    public String getLastActivityStatus(Long userId) {
        // Nếu đang online
        if (isOnline(userId)) {
            return "Đang hoạt động";
        }

        // Lấy thời gian cuối từ lastSeen hoặc lastActivity
        LocalDateTime lastTime = lastSeen.get(userId);
        if (lastTime == null) {
            lastTime = lastActivity.get(userId);
        }

        if (lastTime == null) {
            return "Không xác định";
        }

        return formatTimeAgo(lastTime);
    }

    // Format thời gian thành text như Facebook
    private String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days == 1) {
            return "Hôm qua";
        } else if (days < 7) {
            return days + " ngày trước";
        } else {
            return "Lâu rồi";
        }
    }

    // Lấy danh sách user online (để hiển thị trong sidebar)
    public Map<Long, String> getOnlineUsers() {
        Map<Long, String> result = new ConcurrentHashMap<>();
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);

        lastActivity.entrySet().stream()
                .filter(entry -> entry.getValue().isAfter(cutoff))
                .forEach(entry -> result.put(entry.getKey(), "Đang hoạt động"));

        return result;
    }

    // Cleanup - xóa dữ liệu cũ (gọi định kỳ)
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        lastActivity.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        lastSeen.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }
}