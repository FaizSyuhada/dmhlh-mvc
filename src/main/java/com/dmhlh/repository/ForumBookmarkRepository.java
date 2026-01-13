package com.dmhlh.repository;

import com.dmhlh.entity.ForumBookmark;
import com.dmhlh.entity.ForumThread;
import com.dmhlh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ForumBookmarkRepository extends JpaRepository<ForumBookmark, Long> {
    Optional<ForumBookmark> findByThreadAndUser(ForumThread thread, User user);
    boolean existsByThreadAndUser(ForumThread thread, User user);
    void deleteByThreadAndUser(ForumThread thread, User user);
    List<ForumBookmark> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT fb.thread.id FROM ForumBookmark fb WHERE fb.user.id = :userId")
    Set<Long> findThreadIdsByUserId(Long userId);
}
