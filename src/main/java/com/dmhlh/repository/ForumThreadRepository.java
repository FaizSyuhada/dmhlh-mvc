package com.dmhlh.repository;

import com.dmhlh.entity.ForumThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    
    List<ForumThread> findByStatusOrderByPinnedDescCreatedAtDesc(ForumThread.Status status);
    
    // Hot: ordered by engagement (likes + replies + views) with recency factor
    @Query("SELECT t FROM ForumThread t LEFT JOIN FETCH t.tags WHERE t.status = 'ACTIVE' " +
           "ORDER BY t.pinned DESC, (t.likeCount * 3 + t.viewCount + t.replyCount * 2) DESC, t.updatedAt DESC")
    List<ForumThread> findActiveThreads();
    
    // New: ordered by creation time
    @Query("SELECT t FROM ForumThread t LEFT JOIN FETCH t.tags WHERE t.status = 'ACTIVE' " +
           "ORDER BY t.pinned DESC, t.createdAt DESC")
    List<ForumThread> findActiveThreadsByNew();
    
    // Top: ordered by like count
    @Query("SELECT t FROM ForumThread t LEFT JOIN FETCH t.tags WHERE t.status = 'ACTIVE' " +
           "ORDER BY t.pinned DESC, t.likeCount DESC, t.createdAt DESC")
    List<ForumThread> findActiveThreadsByPopularity();
    
    // Bookmarked by user
    @Query("SELECT t FROM ForumThread t LEFT JOIN FETCH t.tags " +
           "JOIN ForumBookmark b ON b.thread = t WHERE b.user.id = :userId AND t.status = 'ACTIVE' " +
           "ORDER BY b.createdAt DESC")
    List<ForumThread> findBookmarkedByUser(@Param("userId") Long userId);
    
    // Filter by tag
    @Query("SELECT t FROM ForumThread t LEFT JOIN FETCH t.tags tg WHERE t.status = 'ACTIVE' AND tg.name = :tagName " +
           "ORDER BY t.pinned DESC, t.updatedAt DESC")
    List<ForumThread> findByTagName(@Param("tagName") String tagName);
    
    // Count posts for a thread
    @Query("SELECT COUNT(p) FROM ForumPost p WHERE p.thread.id = :threadId AND p.status = 'ACTIVE'")
    int countActivePostsByThreadId(@Param("threadId") Long threadId);
    
    List<ForumThread> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    @Query("SELECT COUNT(t) FROM ForumThread t WHERE t.status = :status")
    long countByStatus(@Param("status") ForumThread.Status status);
    
    long countByAuthorId(Long authorId);
    
    // Find threads by status (for moderation)
    List<ForumThread> findByStatus(ForumThread.Status status);
    
    // Find pending moderation threads ordered by creation
    @Query("SELECT t FROM ForumThread t WHERE t.status = 'PENDING_MODERATION' ORDER BY t.createdAt ASC")
    List<ForumThread> findPendingModerationThreads();
}
