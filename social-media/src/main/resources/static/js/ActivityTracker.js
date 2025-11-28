class ActivityTracker {
    constructor() {
        this.pingInterval = null;
        this.isActive = true;
        this.lastActivityTime = Date.now();
        this.PING_INTERVAL = 60000; // 1 phút
        this.INACTIVE_THRESHOLD = 300000; // 5 phút

        this.init();
    }

    init() {
        this.startPinging();
        this.setupActivityListeners();
        this.setupVisibilityListener();

        // [MỚI] Thêm chức năng theo dõi Click cho AI
        this.setupAiTracking();
    }

    // --- PHẦN 1: LOGIC ONLINE/OFFLINE (Của bạn) ---
    startPinging() {
        this.pingInterval = setInterval(() => {
            if (this.shouldPing()) {
                this.pingServer();
            }
        }, this.PING_INTERVAL);
    }

    shouldPing() {
        const now = Date.now();
        const timeSinceLastActivity = now - this.lastActivityTime;
        return timeSinceLastActivity < this.INACTIVE_THRESHOLD && this.isActive;
    }

    async pingServer() {
        try {
            // Gọi API Ping (Chỉ để server biết mình còn sống)
            await fetch('/api/activity/ping', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            // console.log('✅ Ping online status'); // Bỏ comment nếu muốn debug
        } catch (error) {
            console.error('Ping error:', error);
        }
    }

    setupActivityListeners() {
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
        events.forEach(event => {
            document.addEventListener(event, () => this.lastActivityTime = Date.now(), true);
        });
    }

    setupVisibilityListener() {
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.isActive = false;
            } else {
                this.isActive = true;
                this.lastActivityTime = Date.now();
                this.pingServer();
            }
        });
    }

    // --- PHẦN 2: LOGIC AI TRACKING (Của mình) ---
    setupAiTracking() {
        // Tìm tất cả bài viết có class 'post-card' (hoặc thẻ bao quanh bài viết)
        // Lưu ý: Bạn nhớ thêm class="post-card" vào thẻ div bài viết trong HTML nhé
        document.addEventListener('click', (e) => {
            // Tìm thẻ cha gần nhất có class 'post-card'
            const postCard = e.target.closest('.post-card');

            if (postCard) {
                const postId = postCard.getAttribute('data-post-id');
                if (postId) {
                    console.log("👆 User quan tâm bài viết: " + postId);
                    this.sendAiLog('CLICK', postId);
                }
            }
        });
    }

    async sendAiLog(type, postId) {
        try {
            await fetch('/api/activity/log', { // Gọi API Log của AI
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    actionType: type,
                    postId: postId
                })
            });
        } catch (error) {
            console.error("AI Tracking Error:", error);
        }
    }
}

// Khởi chạy khi web load xong
document.addEventListener('DOMContentLoaded', () => {
    window.activityTracker = new ActivityTracker();
});