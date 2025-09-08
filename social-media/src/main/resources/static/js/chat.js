// ================ IMPROVED CHAT SYSTEM ================
let stompClient = null;

class ChatManager {
    constructor() {
        this.openChats = new Map();
        this.chatBubbles = new Map();
        this.conversationCache = new Map();
        this.pendingFiles = {};
        this.maxChats = 3;
        this.maxBubbles = 4;
        this.compactBubbleId = 'chat-bubble-compact';

        this.setupContainers();
        this.setupGlobalListeners();
    }

    setupContainers() {
        let bubbles = document.getElementById('chatBubblesContainer');
        if (!bubbles) {
            bubbles = document.createElement('div');
            bubbles.id = 'chatBubblesContainer';
            bubbles.className = 'chat-bubbles-container';
            document.body.appendChild(bubbles);
        }
        let windows = document.getElementById('chatWindowsContainer');
        if (!windows) {
            windows = document.createElement('div');
            windows.id = 'chatWindowsContainer';
            windows.className = 'chat-windows-container';
            document.body.appendChild(windows);
        }
    }

    setupGlobalListeners() {
        document.addEventListener('click', (e) => {
            const dd = document.getElementById('messageDropdown');
            const icon = document.getElementById('messageIcon');
            if (dd && icon && !dd.contains(e.target) && !icon.contains(e.target)) {
                dd.classList.remove('show');
            }
        });
    }

    getCurrentUserId() {
        return (
            document.querySelector('meta[name="user-id"]')?.content ||
            document.querySelector('[data-user-id]')?.getAttribute('data-user-id') ||
            '0'
        );
    }

    async openChat(targetKey, displayName, avatar, type = 'private') {

        let chatType = (type || 'private').toLowerCase();
        let domKey = String(targetKey);

        if (chatType === 'private' && this.conversationCache.has(`user:${domKey}`)) {
            const conv = this.conversationCache.get(`user:${domKey}`);
            domKey = String(conv.id);
        }

        if (this.openChats.has(domKey)) {
            const existing = this.openChats.get(domKey);
            existing.style.display = 'flex';
            if (this.chatBubbles.has(domKey)) {
                this.chatBubbles.get(domKey).remove();
                this.chatBubbles.delete(domKey);
                this.updateBubblesCompact();
            }
            return;
        }

        const visible = Array.from(this.openChats.values()).filter(c => c.style.display !== 'none');
        if (visible.length >= this.maxChats) {
            const oldest = visible[0];
            this.minimizeChat(oldest.id.replace('chat-', ''));
        }

        let convDto = null;
        if (chatType === 'private') {
            convDto = await this.findOrCreateConversation(domKey);
            if (!convDto) {
                this.toast('Không thể tạo cuộc trò chuyện', 'error');
                return;
            }
            this.conversationCache.set(String(convDto.id), convDto);
            this.conversationCache.set(`user:${targetKey}`, convDto);
            domKey = String(convDto.id);
        }

        const chatWin = this.createChatWindow(domKey, displayName, avatar, chatType, convDto);
        document.getElementById('chatWindowsContainer').appendChild(chatWin);
        this.openChats.set(domKey, chatWin);
        await this.loadConversationHistory(domKey);

        if (chatType === 'group') this.loadGroupParticipants(domKey);

        // Subscribe to the conversation topic for real-time messages
        this.subscribeToConversation(domKey);
    }

    async openExistingConversation(conversationId, name, avatar, type) {
        const id = String(conversationId);
        console.log("conversationId in openExistingConversation: ", conversationId)
        if (this.openChats.has(id)) {
            const existing = this.openChats.get(id);
            existing.style.display = 'flex';
            if (this.chatBubbles.has(id)) {
                this.chatBubbles.get(id).remove();
                this.chatBubbles.delete(id);
                this.updateBubblesCompact();
            }
            return;
        }
        const visible = Array.from(this.openChats.values()).filter(c => c.style.display !== 'none');
        if (visible.length >= this.maxChats) {
            const oldest = visible[0];
            this.minimizeChat(oldest.id.replace('chat-', ''));
        }
        const chatWin = this.createChatWindow(id, name, avatar, (type || 'private'));
        document.getElementById('chatWindowsContainer').appendChild(chatWin);
        this.openChats.set(id, chatWin);

        setTimeout(() => this.loadConversationHistory(id), 100);
        if ((type || '').toLowerCase() === 'group') this.loadGroupParticipants(id);

        // Subscribe to the conversation topic for real-time messages
        this.subscribeToConversation(id);
    }

    subscribeToConversation(conversationId) {
        if (!stompClient || !stompClient.connected) {
            console.warn('Stomp client not connected');
            return;
        }
        stompClient.subscribe(`/topic/conversation/${conversationId}`, (message) => {
            const messageData = JSON.parse(message.body);
            this.handleIncomingMessage(messageData);
        });
    }

    async findOrCreateConversation(targetUserId) {
        try {
            const res = await fetch('/api/chat/find-or-create-conversation', {
                method: 'POST',
                headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
                body: JSON.stringify({targetUserId: parseInt(targetUserId)})
            });
            const result = await res.json();
            return result.success ? result.conversation : null;
        } catch (e) {
            console.error(e);
            return null;
        }
    }

    createChatWindow(chatId, name, avatar, type = 'private') {
        const wrap = document.createElement('div');
        wrap.className = 'chat-window';
        wrap.id = `chat-${chatId}`;
        wrap.setAttribute('data-conversation-id', chatId);
        wrap.setAttribute('data-conversation-type', type);

        wrap.innerHTML = `
      <div class="chat-header ${type === 'group' ? 'is-group' : ''}">
        <div class="chat-user">
          <img class="chat-avatar ${type === 'group' ? 'group' : ''}" src="${avatar || (type === 'group' ? '/images/default-group-avatar.jpg' : '/images/default-avatar.jpg')}" alt="${name}">
          <div class="meta">
            <div class="name">${name || ''}</div>
            <div class="sub">${type === 'group' ? 'Nhóm' : 'Đang hoạt động'}</div>
          </div>
        </div>
        <div class="chat-ctl"> 
          <button class="chat-btn" title="Thu nhỏ" onclick="chatManager.minimizeChat('${chatId}')"><i class="fa-solid fa-minus"></i></button>
          <button class="chat-btn" title="Đóng" onclick="chatManager.closeChat('${chatId}')"><i class="fa-solid fa-xmark"></i></button>
          ${type === 'group'
            ? `<button class="chat-btn" title="Đổi ảnh nhóm" onclick="chatManager.changeGroupAvatar('${chatId}')"><i class="fa-solid fa-image"></i></button>`
            : ``}
          <button class="chat-btn" title="Video call"  onclick="chatManager.toggleVideo('${chatId}')"><i class="fa-solid fa-video"></i></button>
         </div>
      </div>
      <div class="chat-messages" id="messages-${chatId}">
        <div class="message-time">Hôm nay</div>
      </div>
      <div class="mention-suggestions" id="mentions-${chatId}" style="display:none"></div>
      <div class="chat-input">
        <div class="input-wrap">
          <textarea id="input-${chatId}" rows="1" placeholder="Nhập tin nhắn... ${type === 'group' ? '(Dùng @ để tag)' : ''}"
            data-chat-type="${type}"
            oninput="chatManager.handleInput(event, '${chatId}')"
            onkeydown="chatManager.handleKeyDown(event, '${chatId}')"
            onkeypress="chatManager.handleKeyPress(event,'${chatId}')"></textarea>
          <div class="input-icons">
            <i class="fa-regular fa-face-smile input-icon" onclick="chatManager.toggleEmoji('${chatId}')"></i>
            <i class="fa-solid fa-paperclip input-icon" onclick="chatManager.attachFile('${chatId}')"></i>
            <i class="fa-solid fa-paper-plane input-icon" onclick="chatManager.sendMessage('${chatId}')"></i>
          </div>
        </div>
        <div class="file-preview-box" id="preview-${chatId}"></div>
        
      </div>
      <emoji-picker id="emojiPicker-${chatId}" class="emoji-popup" style="display:none;"></emoji-picker>
    `;
        return wrap;
    }

    minimizeChat(conversationId) {
        const id = String(conversationId);
        const win = this.openChats.get(id);
        if (!win) return;
        win.style.display = 'none';

        const bubble = document.createElement('div');
        bubble.className = 'chat-bubble';
        bubble.dataset.id = id;
        bubble.onclick = () => this.restoreChat(id);

        const name = win.querySelector('.name')?.textContent || '';
        const avatar = win.querySelector('.chat-avatar')?.src || '/images/default-avatar.jpg';
        bubble.innerHTML = `<img src="${avatar}" alt="${name}"><div class="online-indicator"></div>`;

        document.getElementById('chatBubblesContainer').appendChild(bubble);
        this.chatBubbles.set(id, bubble);
        this.updateBubblesCompact();
    }

    restoreChat(conversationId) {
        const id = String(conversationId);
        const win = this.openChats.get(id);
        const bubble = this.chatBubbles.get(id);

        const visible = Array.from(this.openChats.values()).filter(c => c.style.display !== 'none');
        if (visible.length >= this.maxChats) {
            const oldest = visible[0];
            this.minimizeChat(oldest.id.replace('chat-', ''));
        }

        if (win) win.style.display = 'flex';
        if (bubble) {
            bubble.remove();
            this.chatBubbles.delete(id);
        }
        this.updateBubblesCompact();
    }

    closeChat(conversationId) {
        const id = String(conversationId);
        const win = this.openChats.get(id);
        if (win) {
            win.remove();
            this.openChats.delete(id);
        }
        const bubble = this.chatBubbles.get(id);
        if (bubble) {
            bubble.remove();
            this.chatBubbles.delete(id);
        }
        if (this.pendingFiles[id]) delete this.pendingFiles[id];
        this.updateBubblesCompact();
    }

    updateBubblesCompact() {
        const container = document.getElementById('chatBubblesContainer');
        if (!container) return;

        const old = document.getElementById(this.compactBubbleId);
        if (old) old.remove();

        const bubbles = Array.from(container.querySelectorAll('.chat-bubble'));
        if (bubbles.length <= this.maxBubbles) return;

        bubbles.forEach((b, i) => {
            b.style.display = i < (bubbles.length - this.maxBubbles) ? 'none' : 'block';
        });

        const hiddenCount = bubbles.length - this.maxBubbles;
        const compact = document.createElement('div');
        compact.id = this.compactBubbleId;
        compact.className = 'chat-bubble compact';
        compact.textContent = `+${hiddenCount}`;
        compact.title = `${hiddenCount} cuộc trò chuyện`;
        compact.onclick = () => {
            bubbles.forEach(b => b.style.display = 'block');
            compact.remove();
        };
        container.insertBefore(compact, container.firstChild);
    }

    async sendMessage(chatId) {
        const input = document.getElementById(`input-${chatId}`);
        if (!input) return;
        const msg = (input.value || '').trim();

        // Lấy file pending
        const files = this.pendingFiles[chatId] || [];

        if (!msg && files.length === 0) return; // tránh gửi rỗng

        const formData = new FormData();
        formData.append("conversationId", chatId);
        formData.append("content", msg);

        files.forEach(f => formData.append("files", f));

        try {
            const res = await fetch('/api/chat/send-message', {
                method: 'POST',
                body: formData
            });
            if (!res.ok) {
                const result = await res.json();
                throw new Error(result.error || 'Unknown error');
            }
            const result = await res.json();
            if (!result.success) throw new Error(result.error);

            // Clear input + preview + pending
            input.value = '';
            delete this.pendingFiles[chatId];
            const previewBox = document.getElementById(`preview-${chatId}`);
            if (previewBox) previewBox.innerHTML = '';
        } catch (e) {
            console.error(e);
            this.toast('Không thể gửi tin nhắn: ' + e.message, 'error');
        }
    }

    async loadConversationHistory(conversationId) {
        try {
            const res = await fetch(`/api/chat/conversation/${conversationId}/messages?page=0&size=20`);
            if (!res.ok) throw new Error('Failed to load history');
            let messages = await res.json();
            const box = document.getElementById(`messages-${conversationId}`);
            if (!box) return;
            const timeDiv = box.querySelector('.message-time');
            box.innerHTML = '';
            if (timeDiv) box.appendChild(timeDiv);
            const me = String(this.getCurrentUserId());

            (messages || []).forEach(m => {
                const mine = String(m.senderId) === me;
                if (mine) {
                    this.addMessageToUI(conversationId, m, 'sent');
                } else {
                    this.addMessageToUI(conversationId, m, 'received', {
                        name: m.senderName,
                        avatar: m.senderAvatar
                    });
                }
            });

            box.scrollTop = box.scrollHeight;
        } catch (e) {
            console.error(e);
            this.toast('Lỗi tải lịch sử chat', 'error');
        }
    }

    addMessageToUI(conversationId, message, type, sender = null) {
        const box = document.getElementById(`messages-${conversationId}`);
        if (!box) return;

        const row = document.createElement('div');
        row.className = `message ${type}`;
        const el = this.renderMessage(message);

        if (type === 'received' && sender) {
            row.innerHTML = `
            <img class="message-avatar" src="${sender.avatar || '/images/default-avatar.jpg'}" alt="${sender.name}">
            <div class="message-content" >
                <div class="message-sender">${sender.name}</div>
                ${el}
            </div>`;
        } else {
            row.innerHTML = `
            <div class="message-content">
                ${el}
            </div>`;
        }

        box.appendChild(row);
        box.scrollTop = box.scrollHeight;
    }


    renderMessage(message) {
        let html = '';

        // Render attachments (multiple supported)
        (message.attachments || []).forEach(att => {
            switch (att.type) {
                case "IMAGE":
                    html += `<img src="${att.attachmentUrl}" alt="image" class="message-image">`;
                    break;
                case "VIDEO":
                    html += `<video src="${att.attachmentUrl}" controls class="message-video"></video>`;
                    break;
                case "AUDIO":
                    html += `<audio controls src="${att.attachmentUrl}" class="message-audio"></audio>`;
                    break;
                case "FILE":
                    const ext = att.attachmentUrl.split('.').pop().toLowerCase();
                    html += `
                        <div class="message-file">
                              <a href="${att.attachmentUrl}" download="${att.fileName}" class="file-link">
                                <div class="file-icon ${ext}"></div>
                                <div class="file-info">
                                  <div class="file-name">${att.fileName}</div>
                                  <div class="file-size">${att.fileSize}</div>
                                </div>
                              </a>
                            </div>
                                    `;
                    break;
                default:
                    html += `<div class="message-unknown">[Unsupported attachment]</div>`;
            }
        });

        // If CALL type without attachments/content
        if (message.type === "CALL" && !html) {
            html = `<div class="message-call">📞 Cuộc gọi: ${message.content || 'Không xác định'}</div>`;
        }

        // Render content if present (TEXT or CALL description)
        if (message.content) {
            html += `<div class="message-text">${ChatManager.processMentions(message.content)}</div>`;
        }

        return html || `<div class="message-unknown">[Empty message]</div>`;
    }

    toggleVideo(conversation_id) {  // Assume conversation_id is passed dynamically from chat window
        const url = `/video_call/${conversation_id}`;
        window.open(url, `VideoPopup${conversation_id}`, 'width=1070,height=600,resizable=yes,scrollbars=no');

        // Send startCall to trigger invites (no need for isCaller here)
        stompClient.send("/app/startCall", {}, JSON.stringify({
            conversationId: conversation_id
        }));
    }

    toggleEmoji(chatId) {
        const picker = document.getElementById(`emojiPicker-${chatId}`);
        picker.style.display = picker.style.display === 'none' ? 'block' : 'none';

        // Chỉ gắn listener 1 lần
        if (!picker.dataset.bound) {
            picker.addEventListener('emoji-click', (event) => {
                const emoji = event.detail.unicode;
                const textarea = document.getElementById(`input-${chatId}`);

                // Chèn vào đúng vị trí con trỏ
                const start = textarea.selectionStart;
                const end = textarea.selectionEnd;
                const text = textarea.value;

                textarea.value = text.slice(0, start) + emoji + text.slice(end);
                textarea.selectionStart = textarea.selectionEnd = start + emoji.length;
                textarea.focus();
            });
            picker.dataset.bound = "true";
        }
    }

    // Trong class ChatManager (giả sử bạn dùng class)
    async attachFile(chatId) {
        const input = document.createElement('input');
        input.type = 'file';
        input.multiple = true;
        input.accept = 'image/*,video/*,.pdf,.doc,.docx';
        input.onchange = (e) => {
            const files = Array.from(e.target.files);
            if (!files.length) return;
            if (files.length > 10) {  // Giới hạn 10 files per send để hiệu suất
                this.toast('Tối đa 10 files mỗi lần gửi', 'error');
                return;
            }

            if (!this.pendingFiles[chatId]) {
                this.pendingFiles[chatId] = [];
            }
            this.pendingFiles[chatId].push(...files);

            // Hiển thị preview
            const previewBox = document.getElementById(`preview-${chatId}`);
            if (previewBox) {
                previewBox.innerHTML = '';  // Clear trước khi add new

                files.forEach((f, index) => {
                    const ext = f.name.split('.').pop().toLowerCase();

                    // Tạo khối preview
                    const div = document.createElement("div");
                    div.className = "file-preview";

                    let previewEl;

                    if (f.type.startsWith("image")) {
                        // Hiển thị thumbnail ảnh
                        previewEl = document.createElement("img");
                        previewEl.src = URL.createObjectURL(f);
                        previewEl.className = "preview-thumb";
                    } else if (f.type.startsWith("video")) {
                        // Hiển thị video player nhỏ
                        previewEl = document.createElement("video");
                        previewEl.src = URL.createObjectURL(f);
                        previewEl.className = "preview-video";
                        previewEl.controls = true;
                    } else if (f.type.startsWith("audio")) {
                        // Hiển thị audio player
                        previewEl = document.createElement("audio");
                        previewEl.src = URL.createObjectURL(f);
                        previewEl.className = "preview-audio";
                        previewEl.controls = true;
                    } else {
                        // File thường -> icon theo extension
                        let iconPath = "/icons/file.png";
                        if (["pdf"].includes(ext)) iconPath = "/icons/pdf.png";
                        else if (["doc", "docx"].includes(ext)) iconPath = "/icons/word.png";
                        else if (["xls", "xlsx"].includes(ext)) iconPath = "/icons/excel.png";
                        else if (["zip", "rar", "7z"].includes(ext)) iconPath = "/icons/zip.png";

                        previewEl = document.createElement("img");
                        previewEl.src = iconPath;
                        previewEl.className = "file-icon";
                    }

                    // Thông tin file
                    const info = document.createElement("span");
                    info.innerText = `${f.name} (${(f.size / 1024).toFixed(1)} KB)`;

                    // Nút xoá
                    const removeBtn = document.createElement("button");
                    removeBtn.className = "remove-btn";
                    removeBtn.innerText = "✖";
                    removeBtn.title = "Xóa file này";
                    removeBtn.onclick = () => {
                        files.splice(index, 1);   // Xoá khỏi mảng
                        div.remove();             // Xoá khỏi UI
                    };

                    div.appendChild(previewEl);
                    div.appendChild(info);
                    div.appendChild(removeBtn);
                    previewBox.appendChild(div);
                });
            }
        };
        input.click();
    }

// Hàm helper để xác định type (not used in JS, but kept for reference)
    getMessageType(mimeType) {
        if (mimeType.startsWith('image/')) return 'IMAGE';
        if (mimeType.startsWith('video/')) return 'VIDEO';
        if (mimeType.startsWith('audio/')) return 'AUDIO';
        return 'FILE';
    }

    showErrorMessage(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-notification';
        errorDiv.textContent = message;
        errorDiv.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            background: #dc3545;
            color: white;
            padding: 12px 16px;
            border-radius: 4px;
            z-index: 10001;
            animation: slideInRight 0.3s ease;
        `;

        document.body.appendChild(errorDiv);

        setTimeout(() => {
            if (errorDiv.parentNode) {
                errorDiv.remove();
            }
        }, 3000);
    }

    // Load online friends for sidebar
    loadOnlineFriends() {
        const el = document.getElementById('onlineFriendsList');
        if (!el) return;

        fetch('/api/chat/online-friends')
            .then(r => r.json())
            .then(data => {
                const friends = data; // ✅ chỉ lấy friends

                if (!Array.isArray(friends) || friends.length === 0) {
                    el.innerHTML = `<div class="text-center p-3 text-muted">Không có bạn bè online</div>`;
                    return;
                }

                el.innerHTML = friends.map(f => `
                <div class="friend-item-enhanced" 
                     onclick="chatManager.openExistingConversation('${f.id}', '${this.escape(f.name)}', '${f.avatar || '/images/default-avatar.jpg'}', '${f.type || 'private'}')">
                  <div style="position:relative">
                    <img src="${f.avatar || '/images/default-avatar.jpg'}" class="friend-avatar">
                    ${(f.isOnline || f.online) ? '<div class="online-indicator"></div>' : ''}
                  </div>
                  <span class="friend-name">${this.escape(f.name)}</span>
                </div>
            `).join('');
            })
            .catch(() => {
                el.innerHTML = `<div class="text-center text-danger p-3">Lỗi tải danh sách</div>`;
            });
    }

    handleInput(evt, convId) {
        const ta = evt.target;
        this.autoResize(ta);
        if ((ta.dataset.chatType || 'private') !== 'group') return;

        const val = ta.value;
        const cursor = ta.selectionStart;
        const before = val.substring(0, cursor);
        const match = before.match(/@(\w*)$/);
        if (match) {
            this._mentionStart = before.lastIndexOf('@');
            this._mentionQuery = match[1];
            this.showMentionSuggestions(convId, this._mentionQuery);
        } else {
            this.hideMentionSuggestions(convId);
            this._mentionStart = -1;
        }
    }

    handleKeyDown(evt, convId) {
        if (evt.key === 'Enter' && !evt.shiftKey) return;
        const box = document.getElementById(`mentions-${convId}`);
        if (!box || box.style.display !== 'block') return;
        const items = box.querySelectorAll('.mention-item');
        let idx = Array.from(items).findIndex(x => x.classList.contains('selected'));
        switch (evt.key) {
            case 'ArrowDown':
                evt.preventDefault();
                idx = Math.min(idx + 1, items.length - 1);
                this._selectMentionItem(items, idx);
                break;
            case 'ArrowUp':
                evt.preventDefault();
                idx = Math.max(idx - 1, 0);
                this._selectMentionItem(items, idx);
                break;
            case 'Tab':
            case 'Enter':
                if (idx >= 0) {
                    evt.preventDefault();
                    this.selectMention(convId, items[idx]);
                }
                break;
            case 'Escape':
                this.hideMentionSuggestions(convId);
                break;
        }
    }

    handleKeyPress(evt, convId) {
        if (evt.key === 'Enter' && !evt.shiftKey) {
            evt.preventDefault();
            this.sendMessage(convId);
        }
    }

    async loadGroupParticipants(conversationId) {
        try {
            const res = await fetch(`/api/chat/conversation/${conversationId}/participants`);
            const arr = await res.json();
            this._mentionPool = this._mentionPool || new Map();
            this._mentionPool.set(conversationId, arr || []);
            const sub = document.querySelector(`#chat-${conversationId} .sub`);
            if (sub) sub.textContent = `${(arr || []).length} thành viên`;
        } catch {
        }
    }

    showMentionSuggestions(convId, query) {
        const pool = (this._mentionPool && this._mentionPool.get(convId)) || [];
        if (!pool.length) {
            this.loadGroupParticipants(convId);
            return;
        }
        const filtered = pool.filter(p =>
            (p.fullName || '').toLowerCase().includes(query.toLowerCase()) ||
            (p.username || '').toLowerCase().includes(query.toLowerCase())
        );
        if (!filtered.length) {
            this.hideMentionSuggestions(convId);
            return;
        }
        const el = document.getElementById(`mentions-${convId}`);
        el.innerHTML = filtered.map((p, i) => `
      <div class="mention-item ${i === 0 ? 'selected' : ''}" data-username="${p.username}" onclick="chatManager.selectMention('${convId}', this)">
        <img class="mention-avatar" src="${p.avatar || '/images/default-avatar.jpg'}">
        <div class="mention-info">
          <div class="mention-name">${this.escape(p.fullName)}</div>
          <div class="mention-username">@${this.escape(p.username || '')}</div>
        </div>
        ${p.role === 'ADMIN' ? '<i class="fa-solid fa-crown mention-admin"></i>' : ''}
      </div>
    `).join('');
        el.style.display = 'block';
    }

    _selectMentionItem(items, idx) {
        items.forEach((it, i) => it.classList.toggle('selected', i === idx));
    }

    selectMention(convId, el) {
        const ta = document.getElementById(`input-${convId}`);
        const username = el.getAttribute('data-username');
        const val = ta.value;
        const before = val.substring(0, this._mentionStart);
        const after = val.substring(ta.selectionStart);
        const mention = `@${username} `;
        ta.value = before + mention + after;
        const pos = before.length + mention.length;
        ta.setSelectionRange(pos, pos);
        this.hideMentionSuggestions(convId);
        ta.focus();
        this.autoResize(ta);
    }

    hideMentionSuggestions(convId) {
        const el = document.getElementById(`mentions-${convId}`);
        if (el) el.style.display = 'none';
    }

    static processMentions(text) {
        return String(text || '').replace(/@(\w+)/g, (_, u) => `<span class="mention-tag">@${u}</span>`);
    }

    changeGroupAvatar(conversationId) {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*';
        input.onchange = async (e) => {
            const f = e.target.files?.[0];
            if (!f) return;
            const form = new FormData();
            form.append('avatar', f);
            try {
                const res = await fetch(`/api/chat/conversation/${conversationId}/avatar`, {
                    method: 'POST',
                    body: form
                });
                const result = await res.json();
                if (result.success) {
                    const img = document.querySelector(`#chat-${conversationId} .chat-avatar`);
                    if (img) img.src = result.avatarUrl;
                    this.toast('Cập nhật ảnh nhóm thành công!', 'success');
                } else this.toast(result.error || 'Không thể cập nhật ảnh nhóm', 'error');
            } catch {
                this.toast('Lỗi cập nhật ảnh nhóm', 'error');
            }
        };
        input.click();
    }

    autoResize(ta) {
        ta.style.height = 'auto';
        ta.style.height = Math.min(ta.scrollHeight, 100) + 'px';
    }

    escape(s) {
        return String(s || '')
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    toast(msg, type = 'info') {
        const bg = type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#1877f2';
        const el = document.createElement('div');
        el.style.cssText = `position:fixed;top:80px;right:20px;background:${bg};color:#fff;padding:10px 14px;border-radius:8px;z-index:13000;box-shadow:0 8px 24px rgba(0,0,0,.15)`;
        el.textContent = msg;
        document.body.appendChild(el);
        setTimeout(() => el.remove(), 2500);
    }

    handleIncomingMessage(payload) {
        const id = String(payload.conversationId);
        const me = String(this.getCurrentUserId());
        const mine = String(payload.senderId) === me;
        if (this.openChats.has(id)) {
            if (mine) {
                this.addMessageToUI(id, payload, 'sent');
            } else {
                this.addMessageToUI(id, payload, 'received', {
                    name: payload.senderName,
                    avatar: payload.senderAvatar
                });
            }
        } else {
            // có thể hiện badge/notification nếu muốn
        }
    }

}

// Nâng cao (để mở rộng sau): tạo nhóm qua modal, tìm user… (đang dùng các API có sẵn)
class EnhancedChatManager extends ChatManager {
}

function connectStompClient() {
    if (stompClient && stompClient.connected) return;

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        console.log("✅ Global Stomp connected");

        // Subscribe for call invites
        stompClient.subscribe("/user/queue/call-invite", (message) => {
            console.log("📩 Received call invite:", message.body);
            const invite = JSON.parse(message.body);
            const url = `/video_call/${invite.conversationId}?isIncoming=true&callerId=${invite.callerId}`;
            window.open(url, `VideoPopup${invite.conversationId}`, 'width=1070,height=600,resizable=yes,scrollbars=no');
        });
    }, (error) => {
        console.error("Stomp connection error:", error);
    });
}

async function openChat(userId, name, avatar) {
    try {
        const response = await fetch('/api/chat/find-or-create-conversation', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({targetUserId: userId})
        });

        const data = await response.json();

        if (data.success && data.conversation) {
            const convID = data.conversation.id;
            if (window.chatManager) {
                chatManager.openExistingConversation(convID, name, avatar, 'private');
            }
        } else {
            console.error("Không thể mở chat:", data.error);
        }
    } catch (err) {
        console.error("Lỗi khi mở chat:", err);
    }
}


let chatManager;
document.addEventListener('DOMContentLoaded', () => {
    window.chatManager = chatManager = new EnhancedChatManager();
    connectStompClient();
    if (document.getElementById('onlineFriendsList')) chatManager.loadOnlineFriends();

    bindHeaderDropdown();
});

// Dropdown tin nhắn như FB (tải /api/conversations)
function bindHeaderDropdown() {
    const icon = document.getElementById('messageIcon');
    const dropdown = document.getElementById('messageDropdown');
    const list = document.getElementById('conversationList');
    if (!icon || !dropdown || !list) return;

    let loaded = false;

    const openDropdown = async () => {
        dropdown.classList.add('show');
        const rect = icon.getBoundingClientRect();
        dropdown.style.top = `${rect.bottom + 8 + window.scrollY}px`;
        dropdown.style.right = `${Math.max(16, window.innerWidth - rect.right)}px`;

        if (!loaded) {
            list.innerHTML = `<div class="text-center p-3"><i class="fa-solid fa-spinner fa-spin"></i> Đang tải...</div>`;
            try {
                const r = await fetch('/api/conversations', {headers: {'X-Requested-With': 'XMLHttpRequest'}});
                const data = await r.json();
                if (!Array.isArray(data) || data.length === 0) {
                    list.innerHTML = `<div class="text-center text-muted p-3">Chưa có cuộc trò chuyện</div>`;
                    loaded = true;
                    return;
                }

                list.innerHTML = data.map(c => `
          <div class="conversation-item" data-id="${c.id}" data-type="${c.type}">
            <img src="${c.avatar || (String(c.type).toLowerCase() === 'group' ? '/images/default-group-avatar.jpg' : '/images/default-avatar.jpg')}" alt="">
            <div class="info">
              <div class="conv-name">${c.name || ''}</div>
              <div class="conv-sub">${c.lastMessage ? c.lastMessage : 'Chưa có tin nhắn'}${c.timeAgo ? ' · ' + c.timeAgo : ''}</div>
            </div>
            ${c.hasUnread ? `<span class="badge bg-danger">${c.unreadCount || ''}</span>` : ''}
          </div>`).join('');

                list.querySelectorAll('.conversation-item').forEach(el => {
                    el.addEventListener('click', () => {
                        const id = el.getAttribute('data-id');
                        const type = el.getAttribute('data-type');
                        const name = el.querySelector('.conv-name')?.textContent?.trim() || '';
                        const avatar = el.querySelector('img')?.src || '';
                        dropdown.classList.remove('show');
                        chatManager.openExistingConversation(id, name, avatar, type);
                    });
                });
                loaded = true;
            } catch (e) {
                console.error(e);
                list.innerHTML = `<div class="text-center text-danger p-3"><i class="fa-solid fa-triangle-exclamation me-1"></i> Lỗi tải danh sách</div>`;
            }
        }
    };

    const closeDropdown = () => dropdown.classList.remove('show');

    icon.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (dropdown.classList.contains('show')) closeDropdown();
        else openDropdown();
    });

    icon.setAttribute('tabindex', '0');
    icon.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            if (dropdown.classList.contains('show')) closeDropdown();
            else openDropdown();
        }
    });

    document.addEventListener('click', (e) => {
        if (!dropdown.classList.contains('show')) return;
        if (!dropdown.contains(e.target) && !icon.contains(e.target)) closeDropdown();
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeDropdown();
    });

    document.querySelectorAll("emoji-picker").forEach(picker => {
        picker.addEventListener("emoji-click", event => {
            const emoji = event.detail.unicode;
            const input = picker.closest(".chat-input").querySelector("textarea");
            input.value += emoji;

            // Tự resize textarea nếu cần
            input.dispatchEvent(new Event("input"));
        });
    });

}