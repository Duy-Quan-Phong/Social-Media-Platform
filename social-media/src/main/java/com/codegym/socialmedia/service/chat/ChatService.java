package com.codegym.socialmedia.service.chat;

import com.codegym.socialmedia.dto.chat.ConversationDto;
import com.codegym.socialmedia.dto.chat.MessageDto;
import com.codegym.socialmedia.dto.chat.SendMessageRequest;
import com.codegym.socialmedia.dto.chat.UserSearchDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.conversation.Conversation;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    List<ConversationDto> getConversationsForUser(Long userId);
    List<MessageDto> getChatHistory(Long userId1, Long userId2);
    MessageDto sendMessage(Long senderId, SendMessageRequest request);
    List<ConversationDto> getOnlineFriends(Long userId);

    Conversation createGroupConversation(User creator, List<User> participants, String groupName);

    ConversationDto findOrCreatePrivateConversation(Long currentUserId, Long targetUserId);

    List<MessageDto> getMessagesByConversation(Long conversationId, int page, int size);
    List<ConversationDto.ParticipantDto> getParticipants(Long conversationId);
    ConversationDto createGroupFromIds(Long creatorId, List<Long> participantIds, String groupName);

    String updateGroupAvatar(Long conversationId, MultipartFile file);

    // ====== BỔ SUNG ======
    List<UserSearchDto> searchUsers(String keyword, Long currentUserId);
    List<ConversationDto> getGroupsForUser(Long userId);
}
