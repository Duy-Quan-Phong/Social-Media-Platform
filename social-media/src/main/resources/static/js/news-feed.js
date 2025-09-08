// 1. Cập nhật news-feed.js - thêm loadGroupChats function
class NewsFeedManager {
    constructor() {
        this.init();
    }

    init() {
        this.loadOnlineFriends();
        this.loadGroupChats(); // Thêm dòng này
    }

    async loadOnlineFriends() {
        const el = document.getElementById('onlineFriendsList');
        if (!el) return;

        try {
            const response = await fetch('/api/chat/online-friends');
            const friends = await response.json();

            if (!Array.isArray(friends) || friends.length === 0) {
                el.innerHTML = `<div class="text-center p-3 text-muted">Không có bạn bè online</div>`;
                return;
            }

            el.innerHTML = friends.map(f => `
                <div class="friend-item-enhanced" onclick="openChat('${f.id}', '${this.escape(f.name)}', '${f.avatar || '/images/default-avatar.jpg'}', 'private')">
                    <div style="position:relative">
                        <img src="${f.avatar || '/images/default-avatar.jpg'}" class="friend-avatar">
                        ${(f.isOnline || f.online) ? '<div class="online-indicator"></div>' : ''}
                    </div>
                    <span class="friend-name">${this.escape(f.name)}</span>
                </div>`).join('');
        } catch (error) {
            console.error('Error loading friends:', error);
            el.innerHTML = `<div class="text-center text-danger p-3">Lỗi tải danh sách</div>`;
        }
    }

    // THÊM FUNCTION MỚI: Load Group Chats
    async loadGroupChats() {
        const el = document.getElementById('groupChatsList');
        if (!el) return;

        try {
            const response = await fetch('/api/chat/contacts');
            const data = await response.json();

            if (!data.groups || data.groups.length === 0) {
                el.innerHTML = `<div class="text-center p-3 text-muted">Chưa có nhóm nào</div>`;
                return;
            }

            el.innerHTML = data.groups.map(group => `
                <div class="group-item-enhanced" onclick="openGroup('${group.id}', '${this.escape(group.name)}', '${group.avatar || '/images/default-group-avatar.jpg'}')">
                    <div style="position:relative">
                        <img src="${group.avatar || '/images/default-group-avatar.jpg'}" class="group-avatar">
                        <div class="group-member-count">${group.participantCount || 0}</div>
                    </div>
                    <div class="group-info">
                        <span class="group-name">${this.escape(group.name)}</span>
                        <div class="group-last-message">${group.lastMessage || 'Chưa có tin nhắn'}</div>
                        ${group.timeAgo ? `<div class="group-time">${group.timeAgo}</div>` : ''}
                    </div>
                </div>`).join('');
        } catch (error) {
            console.error('Error loading groups:', error);
            el.innerHTML = `<div class="text-center text-danger p-3">Lỗi tải nhóm</div>`;
        }
    }

    escape(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
}

// Global functions for chat integration
function openChat(userId, name, avatar, type = 'private') {
    if (window.chatManager) {
        chatManager.openChat(userId, name, avatar, type);
    }
}

function openGroup(conversationId, name, avatar) {
    if (window.chatManager) {
        chatManager.openExistingConversation(conversationId, name, avatar, 'group');
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    window.newsFeedManager = new NewsFeedManager();
});