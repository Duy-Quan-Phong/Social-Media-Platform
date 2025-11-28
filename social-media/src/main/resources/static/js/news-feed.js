class NewsFeedManager {
    constructor() {
        this.init();
    }

    init() {
        this.loadGroupChats();
    }

    // Load nhóm
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

            el.innerHTML = data.groups.map(group => {
                const spanStyle = group.unreadCount && group.unreadCount > 0 ? '' : 'display: none;';
                const onlineClass = group.online ? 'online' : 'offline';
                return `
                    <div class="group-item-enhanced"
                         data-id="${group.id}" 
                         data-name="${this.escape(group.name)}"
                         data-avatar="${group.avatar || '/images/default-group-avatar.jpg'}"
                         data-type="group">
                        <div style="position:relative">
                            <img src="${group.avatar || '/images/default-group-avatar.jpg'}" class="group-avatar">
                            <div class="group-member-count">${group.participantCount || 0}</div>
                            <div class="online-dot ${onlineClass}"></div>
                        </div>
                        <div class="group-info">
                            <span class="group-name">${this.escape(group.name)}</span>
                            <div class="group-last-message">${group.lastMessage || 'Chưa có tin nhắn'}</div>
                            ${group.timeAgo ? `<div class="group-time">${group.timeAgo}</div>` : ''}
                            <div class="user-activity-status ${onlineClass}">
                                <span class="activity-dot"></span>
                                <span>${group.online ? 'Có thành viên online' : 'Tất cả offline'}</span>
                            </div>
                        </div>
                         <span class="badge bg-danger ms-2 span-conversation-id-${group.id}" style="${spanStyle}">
                            ${group.unreadCount || ''}
                         </span>
                    </div>
                `;
            }).join('');

            // Gắn sự kiện click sau khi render
            el.querySelectorAll('.group-item-enhanced').forEach(item => {
                item.addEventListener('click', () => {
                    const { id, name, avatar } = item.dataset;
                    this.openGroup(id, name, avatar);
                });
            });

        } catch (error) {
            console.error('Error loading groups:', error);
            el.innerHTML = `<div class="text-center text-danger p-3">Lỗi tải nhóm</div>`;
        }
    }

    // Escape string
    escape(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    // Gọi hàm chat
    openChat(userId, name, avatar, type = 'private') {
        if (window.chatManager) {
            chatManager.openChat(userId, name, avatar, type);
        }
    }

    openGroup(conversationId, name, avatar) {
        if (window.chatManager) {
            chatManager.openExistingConversation(conversationId, name, avatar, 'group');
        }
    }
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    window.newsFeedManager = new NewsFeedManager();
});