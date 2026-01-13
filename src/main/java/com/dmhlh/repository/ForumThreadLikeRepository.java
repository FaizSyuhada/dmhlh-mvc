package com.dmhlh.repository;

import com.dmhlh.entity.ForumThread;
import com.dmhlh.entity.ForumThreadLike;
import com.dmhlh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ForumThreadLikeRepository extends JpaRepository<ForumThreadLike, Long> {
    Optional<ForumThreadLike> findByThreadAndUser(ForumThread thread, User user);
    boolean existsByThreadAndUser(ForumThread thread, User user);
    void deleteByThreadAndUser(ForumThread thread, User user);
    long countByThread(ForumThread thread);
    
    @Query("SELECT ftl.thread.id FROM ForumThreadLike ftl WHERE ftl.user.id = :userId")
    Set<Long> findThreadIdsByUserId(Long userId);
}
