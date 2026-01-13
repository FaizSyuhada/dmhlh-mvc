package com.dmhlh.repository;

import com.dmhlh.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    
    List<ForumPost> findByThreadIdAndStatusOrderByCreatedAtAsc(Long threadId, ForumPost.Status status);
    
    List<ForumPost> findByThreadIdOrderByCreatedAtAsc(Long threadId);
    
    List<ForumPost> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    long countByThreadId(Long threadId);
    
    // Find posts by status (for moderation)
    List<ForumPost> findByStatus(ForumPost.Status status);
    
    // Count posts by status
    long countByStatus(ForumPost.Status status);
}
