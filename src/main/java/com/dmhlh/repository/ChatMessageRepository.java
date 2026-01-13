package com.dmhlh.repository;

import com.dmhlh.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);
    
    List<ChatMessage> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<ChatMessage> findTop50ByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);
}
