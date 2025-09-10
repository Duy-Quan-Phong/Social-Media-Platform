package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.conversation.Conversation;
import com.codegym.socialmedia.model.conversation.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("""
                SELECT m FROM Message m
                WHERE m.conversation.id = :conversationId
                ORDER BY m.sentAt ASC, m.messageId ASC
            """)
    Page<Message> findMessagesByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("SELECT MAX(m.messageId) FROM Message m WHERE m.conversation.id = :conversationId")
    Long findLatestMessageId(@Param("conversationId") Long conversationId);

    Message findFirstByConversationIdOrderBySentAtDescMessageIdDesc(Long conversationId);


    // đếm tin NHẬN chưa đọc cho user: chưa có bản ghi MessageRead tương ứng
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.messageId > :lastReadId")
    Long countUnreadMessages(@Param("conversationId") Long conversationId,
                             @Param("lastReadId") Long lastReadId);


    @Query("SELECT COUNT(m) FROM Message m " +
            "JOIN ConversationParticipant cp ON cp.conversation.id = m.conversation.id " +
            "WHERE cp.user.id = :userId AND m.messageId > COALESCE(cp.lastReadMessageId, 0)")
    Long countTotalUnread(@Param("userId") Long userId);
}
