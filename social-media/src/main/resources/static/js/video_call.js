let stompClient;
let peers = {}; // key = senderId, value = RTCPeerConnection
const servers = {iceServers: [{urls: "stun:stun.l.google.com:19302"}]};
let seconds = 0;
let timerInterval;
let isRinging = false;
let baseStream; // stream gốc lấy từ camera/mic
const localVideo = document.querySelector(".localVideo");
const remoteVideo  = document.getElementById("remoteVideo");
const timerElement = document.getElementById("timer");
const micBtn = document.querySelector(".toggle-mic i");
const camBtn = document.querySelector(".toggle-camera i");
let initializing = false;
let isJoined = isCaller;
let isCallConnected = false; // To track if at least one remote is connected
const remoteVideosContainer = document.getElementById("remoteVideosContainer");
async function initStream() {
    if (baseStream || initializing) return baseStream;
    initializing = true;

    try {
        baseStream = await navigator.mediaDevices.getUserMedia({video: true, audio: true});
    } catch (err) {
        console.warn("Không thể mở camera/mic:", err);

        // fallback
        try {
            baseStream = await navigator.mediaDevices.getUserMedia({video: true});
            localVideo.srcObject = baseStream;
        } catch (e1) {
            console.warn("Camera cũng bị chặn:", e1);
        }

        try {
            if (!baseStream) {
                baseStream = await navigator.mediaDevices.getUserMedia({audio: true});
            } else {
                const audioStream = await navigator.mediaDevices.getUserMedia({audio: true});
                audioStream.getTracks().forEach(track => baseStream.addTrack(track));
            }
        } catch (e2) {
            console.warn("Mic cũng bị chặn:", e2);
        }
    }finally {
        initializing = false;
    }

    if (baseStream) localVideo.srcObject = baseStream;

    return baseStream;
}

// Lấy stream clone cho 1 conversation
async function getStreamForConversation() {
    const stream = await initStream();
    const clone = new MediaStream();
    stream.getTracks().forEach(track => {
        clone.addTrack(track.clone());
    });
    return clone;
}

// Toggle camera
function toggleCamera() {
    if (!baseStream) return;
    const videoTrack = baseStream.getVideoTracks()[0];
    if (!videoTrack) return;
    videoTrack.enabled = !videoTrack.enabled;
    const newEnabled = videoTrack.enabled;
    // Toggle on all peers' video senders
    Object.values(peers).forEach(pc => {
        pc.getSenders().forEach(sender => {
            if (sender.track && sender.track.kind === 'video') {
                sender.track.enabled = newEnabled;
            }
        });
    });
    camBtn.classList.toggle("fa-video", videoTrack.enabled);
    camBtn.classList.toggle("fa-video-slash", !videoTrack.enabled);
}

// Toggle mic
function toggleMicro() {
    if (!baseStream) return;
    const audioTrack = baseStream.getAudioTracks()[0];
    if (!audioTrack) return;
    audioTrack.enabled = !audioTrack.enabled;
    const newEnabled = audioTrack.enabled;
    // Toggle on all peers' audio senders
    Object.values(peers).forEach(pc => {
        pc.getSenders().forEach(sender => {
            if (sender.track && sender.track.kind === 'audio') {
                sender.track.enabled = newEnabled;
            }
        });
    });
    micBtn.classList.toggle("fa-microphone", audioTrack.enabled);
    micBtn.classList.toggle("fa-microphone-slash", !audioTrack.enabled);
}

function startTimer() {
    clearInterval(timerInterval);
    seconds = 0;
    timerInterval = setInterval(() => {
        seconds++;
        const mm = String(Math.floor(seconds / 60)).padStart(2, "0");
        const ss = String(seconds % 60).padStart(2, "0");
        timerElement.textContent = `${mm}:${ss}`;
    }, 1000);
}

function playOutgoingRingtone() {
    stopCallingTone();
    document.getElementById("ringtone").play().catch(e => console.log("Autoplay blocked:", e));
}

function playIncomingRingtone() {
    stopCallingTone();
    document.getElementById("ringtoneIncoming").play().catch(e => console.log("Autoplay blocked:", e));
}


function stopCallingTone() {
    document.getElementById("ringtone").pause();
    document.getElementById("ringtone").currentTime = 0;

    document.getElementById("ringtoneIncoming").pause();
    document.getElementById("ringtoneIncoming").currentTime = 0;

    clearInterval(timerInterval);
    timerElement.textContent = "00:00";
    isRinging = false;
}

function hideIncomingElements() {
    const container = document.querySelector('.container');
    if (container) container.style.display = 'none';
    const pulse = document.querySelector('.pulse-container');
    if (pulse) pulse.style.display = 'none';
    document.querySelector('.action-buttons').style.display = 'flex';
}

// end call
document.querySelector('.end-call')?.addEventListener('click', () => {
    stopCallingTone();
    stompClient.send("/app/endCall", {}, JSON.stringify({
        conversationId: conversation_Id,
        rejecterId: currentUserId
    }));
    // Đóng local ngay (nếu server chậm, hoặc an toàn)
    if (baseStream) {
        baseStream.getTracks().forEach(track => track.stop());
    }
});


if (isIncoming) {
    playIncomingRingtone();

    // Accept: Ẩn nút incoming, hiển thị nút thường, init stream, bắt đầu kết nối
    document.querySelector('.accept')?.addEventListener('click', async (e) => {
        e.currentTarget.style.display = 'none';

        hideIncomingElements();

        stopCallingTone();  // Dừng incoming, không cần ring nữa

        await initStream();
        isJoined = true;
        sendSignal("join", null);
    });

} else if (isCaller) {
    // Caller: Chơi outgoing ringtone
    document.addEventListener("DOMContentLoaded", async () => {
        await initStream();
        playOutgoingRingtone();
    });
}

window.addEventListener("beforeunload", () => {
    stopCallingTone();
    if (baseStream) {
        baseStream.getTracks().forEach(track => track.stop());
    }
});

document.querySelector('.toggle-camera')
    ?.addEventListener("click", toggleCamera);
document.querySelector('.toggle-mic')
    ?.addEventListener("click", toggleMicro);

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        console.log("✅ Connected");
        stompClient.subscribe(`/topic/video/${conversation_Id}`, (message) => {
            const signal = JSON.parse(message.body);
            handleSignal(signal);
        });

        stompClient.subscribe("/user/queue/call-end", (message) => {
            const payload = JSON.parse(message.body);
            console.log("📴 Call ended:", payload);

            stopCallingTone();
            if (baseStream) baseStream.getTracks().forEach(t => t.stop());
            Object.values(peers).forEach(pc => pc.close());
            window.close();
        });

        if (isCaller) {
            sendSignal("join", null);
        }

    });

}

function sendSignal(type, data, targetId = null) {
    const payload = {
        type: type,
        senderId: currentUserId,
        data: data
    };
    if (targetId) {
        payload.targetId = targetId;
    }
    stompClient.send(`/app/video/${conversation_Id}`, {}, JSON.stringify(payload));
}

async function handleSignal(message) {
    if (!isJoined) return;
    if (message.senderId === currentUserId) return;
    if (message.targetId && message.targetId !== currentUserId) return;
    switch (message.type) {
        case "join":
            await handleJoin(message.senderId);
            break;
        case "leave":
            handleLeave(message.senderId);
            break;
        case "offer":
            await createAnswer(message.senderId, message.data);
            break;
        case "answer":
            await peers[message.senderId]?.setRemoteDescription(new RTCSessionDescription(message.data));
            break;
        case "candidate":
            await peers[message.senderId]?.addIceCandidate(new RTCIceCandidate(message.data));
            break;
    }
}

async function createPeerConnection(peerId) {
    const pc = new RTCPeerConnection(servers);
    peers[peerId] = pc;

    // Lấy một bản clone từ baseStream cho cuộc hội thoại này
    const conversationStream = await getStreamForConversation();

    conversationStream.getTracks().forEach(track => pc.addTrack(track, conversationStream));

    pc.ontrack = (event) => {
        if (!isCallConnected) {
            isCallConnected = true;
            stopCallingTone();
            hideIncomingElements();  // Hide for caller when first remote connects
            startTimer();
        }
        let videoEl;
        if (participantsCount === 2) {
            // For 1-1, use the main remoteVideo
            remoteVideo.srcObject = event.streams[0];
        } else {
            // For group, create small video elements in container
            if (remoteVideosContainer) remoteVideosContainer.style.display = 'block';  // Show container if group
            videoEl = document.getElementById("remote-" + peerId);
            if (!videoEl) {
                videoEl = document.createElement("video");
                videoEl.id = "remote-" + peerId;
                videoEl.autoplay = true;
                videoEl.playsInline = true;
                videoEl.style.width = "200px";  // Adjust with CSS for better layout (e.g., grid)
                remoteVideosContainer.appendChild(videoEl);  // Append to container for group layout
            }
            videoEl.srcObject = event.streams[0];
        }
    };

    pc.onicecandidate = (event) => {
        if (event.candidate) {
            sendSignal("candidate", event.candidate, peerId);
        }
    };

    return pc;
}

async function handleJoin(peerId) {
    if (peers[peerId]) return;
    const pc = await createPeerConnection(peerId);
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    sendSignal("offer", offer, peerId);
}

function handleLeave(peerId) {
    if (peers[peerId]) {
        peers[peerId].close();
        delete peers[peerId];
        const videoEl = document.getElementById("remote-" + peerId);
        if (videoEl) videoEl.remove();
        if (participantsCount === 2) {
            remoteVideo.srcObject = null;
        }
    }
    // Check if no remotes left
    if (Object.keys(peers).length === 0) {
        isCallConnected = false;
        clearInterval(timerInterval);
        timerElement.textContent = "00:00";
    }
}

async function createAnswer(peerId, offer) {
    const pc = await createPeerConnection(peerId);
    await pc.setRemoteDescription(new RTCSessionDescription(offer));
    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
    sendSignal("answer", answer, peerId);
}

document.addEventListener("DOMContentLoaded", async () => {
    if (participantsCount > 2 && remoteVideo) remoteVideo.style.display = 'none';
    await initStream();
    if (isCaller) {
        playOutgoingRingtone();
    }else {
        playIncomingRingtone();
    }
    connect();
});
