/*<![CDATA[*/
const currentUser = /*[[${#authentication.principal.username}]]*/ 'defaultUser';
const currentUserId = /*[[${#authentication.principal.id}]]*/ '1';

// Set current user ID in meta tag for chat.js
if (document.querySelector('meta[name="user-id"]')) {
    document.querySelector('meta[name="user-id"]').content = currentUserId;
}

// WebSocket connection and like functionality
let stompClient = null;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize WebSocket
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // Subscribe vào các topic cần thiết cho posts
        document.querySelectorAll('.post').forEach(post => {
            const statusId = post.querySelector('.like-btn').getAttribute('data-status-id');
            if (statusId) {
                stompClient.subscribe('/topic/status/' + statusId + '/likes', function (message) {
                    const data = JSON.parse(message.body);
                    if (data.userName === currentUser) {
                        updateLikeUI(data.statusId, data.likeCount, data.isLiked);
                    } else {
                        const btn = document.querySelector(`.like-btn[data-status-id="${data.statusId}"]`);
                        if (btn) {
                            btn.querySelector('.like-count').textContent = data.likeCount;
                        }
                    }
                });
            }
        });

        // Subscribe to personal message queue for chat
        stompClient.subscribe(`/user/${currentUserId}/queue/messages`, function (message) {
            const messageData = JSON.parse(message.body);
            handleIncomingMessage(messageData);
        });

    }, function (error) {
        console.log('WebSocket connection error: ' + error);
    });

    // Setup like button handlers
    document.querySelectorAll('.like-btn').forEach(btn => {
        btn.addEventListener('click', async function () {
            const statusId = this.getAttribute('data-status-id');

            try {
                const response = await fetch(`/api/likes/status/${statusId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                const data = await response.json();

                if (response.ok) {
                    updateLikeUI(data.statusId, data.likeCount, data.isLiked);
                }
            } catch (error) {
                console.error('Error:', error);
            }
        });
    });
});

function updateLikeUI(statusId, likeCount, isLiked) {
    const likeBtn = document.querySelector(`.like-btn[data-status-id="${statusId}"]`);
    if (likeBtn) {
        likeBtn.querySelector('.like-count').textContent = likeCount;

        if (isLiked) {
            likeBtn.classList.add('liked');
        } else {
            likeBtn.classList.remove('liked');
        }
    }
}


/*]]>*/