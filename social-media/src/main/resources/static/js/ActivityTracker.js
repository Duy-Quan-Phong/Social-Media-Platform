// Activity Tracker - Ping server định kỳ để maintain online status
class ActivityTracker {
    constructor() {
        this.pingInterval = null;
        this.isActive = true;
        this.lastActivityTime = Date.now();
        this.PING_INTERVAL = 60000; // 1 phút
        this.INACTIVE_THRESHOLD = 300000; // 5 phút không hoạt động = offline

        this.init();
    }

    init() {
        this.startPinging();
        this.setupActivityListeners();
        this.setupVisibilityListener();
    }

    // Ping server định kỳ
    startPinging() {
        this.pingInterval = setInterval(() => {
            if (this.shouldPing()) {
                this.pingServer();
            }
        }, this.PING_INTERVAL);
    }

    // Kiểm tra có nên ping server không
    shouldPing() {
        const now = Date.now();
        const timeSinceLastActivity = now - this.lastActivityTime;

        // Chỉ ping nếu user còn active (dưới 5 phút không hoạt động)
        return timeSinceLastActivity < this.INACTIVE_THRESHOLD && this.isActive;
    }

    // Gửi ping đến server
    async pingServer() {
        try {
            await fetch('/api/activity/ping', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            console.log('✅ Activity ping sent');
        } catch (error) {
            console.error('❌ Error pinging server:', error);
        }
    }

    // Setup listeners cho các hoạt động user
    setupActivityListeners() {
        const events = [
            'mousedown', 'mousemove', 'keypress',
            'scroll', 'touchstart', 'click'
        ];

        const updateActivity = () => {
            this.lastActivityTime = Date.now();
        };

        events.forEach(event => {
            document.addEventListener(event, updateActivity, true);
        });
    }

    // Setup listener cho visibility change (tab switch)
    setupVisibilityListener() {
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.isActive = false;
                console.log('🔒 User inactive (tab hidden)');
            } else {
                this.isActive = true;
                this.lastActivityTime = Date.now();
                // Ping ngay khi user quay lại
                this.pingServer();
                console.log('🔓 User active (tab visible)');
            }
        });

        // Window focus/blur events
        window.addEventListener('focus', () => {
            this.isActive = true;
            this.lastActivityTime = Date.now();
            this.pingServer();
        });

        window.addEventListener('blur', () => {
            this.isActive = false;
        });
    }

    // Cleanup khi page unload
    cleanup() {
        if (this.pingInterval) {
            clearInterval(this.pingInterval);
        }
    }

    // Manual ping (có thể gọi từ các action quan trọng)
    manualPing() {
        this.lastActivityTime = Date.now();
        this.pingServer();
    }
}

// Khởi tạo activity tracker khi DOM ready
document.addEventListener('DOMContentLoaded', () => {
    window.activityTracker = new ActivityTracker();
});

// Cleanup khi page unload
window.addEventListener('beforeunload', () => {
    if (window.activityTracker) {
        window.activityTracker.cleanup();
    }
});

// Export để có thể gọi từ nơi khác
window.ActivityTracker = ActivityTracker;