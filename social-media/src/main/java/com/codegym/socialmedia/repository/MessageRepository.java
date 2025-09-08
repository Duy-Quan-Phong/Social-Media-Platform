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

//    @Query("""
//        SELECT m FROM Message m
//        WHERE m.conversation.id = :conversationId
//        ORDER BY m.sentAt DESC, m.messageId DESC
//    """)
//    Message findLatestMessageByConversationId(@Param("conversationId") Long conversationId);
Message findFirstByConversationIdOrderBySentAtDescMessageIdDesc(Long conversationId);


    // đếm tin NHẬN chưa đọc cho user: chưa có bản ghi MessageRead tương ứng
    @Query("""
        SELECT COUNT(m) FROM Message m
        LEFT JOIN m.messageReads r
               ON r.user.id = :userId
        WHERE m.conversation.id = :conversationId
          AND m.sender.id <> :userId
          AND r.readId IS NULL
    """)
    long countUnreadMessages(@Param("conversationId") Long conversationId,
                             @Param("userId") Long userId);


    @Query("""
      SELECT m FROM Message m
      WHERE m.conversation.id = :conversationId
      ORDER BY m.sentAt ASC
    """)
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    Message findTop1ByConversationOrderBySentAtDesc(Conversation conversation);

    Page<Message> findByConversation_IdOrderBySentAtAsc(Long conversationId, Pageable pageable);

    Optional<Message> findTopByConversation_IdOrderBySentAtDesc(Long conversationId);
}
