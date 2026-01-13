package com.dmhlh.repository;

import com.dmhlh.entity.ForumTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumTagRepository extends JpaRepository<ForumTag, Long> {
    Optional<ForumTag> findByName(String name);
    
    @Query("SELECT t FROM ForumTag t ORDER BY t.postCount DESC")
    List<ForumTag> findAllOrderByPostCountDesc();
    
    // Get trending tags with actual thread count (active threads only)
    @Query("SELECT t, COUNT(DISTINCT ft.id) as cnt FROM ForumTag t " +
           "LEFT JOIN t.threads ft ON ft.status = 'ACTIVE' " +
           "GROUP BY t.id ORDER BY cnt DESC")
    List<Object[]> findTrendingTagsWithCount();
    
    List<ForumTag> findAllByOrderByNameAsc();
    
    // Update post count for a specific tag
    @Modifying
    @Query("UPDATE ForumTag t SET t.postCount = " +
           "(SELECT COUNT(DISTINCT ft.id) FROM ForumThread ft JOIN ft.tags tg WHERE tg.id = t.id AND ft.status = 'ACTIVE') " +
           "WHERE t.id = :tagId")
    void updatePostCount(@Param("tagId") Long tagId);
    
    // Update all tag counts
    @Modifying
    @Query("UPDATE ForumTag t SET t.postCount = " +
           "(SELECT COUNT(DISTINCT ft.id) FROM ForumThread ft JOIN ft.tags tg WHERE tg.id = t.id AND ft.status = 'ACTIVE')")
    void updateAllPostCounts();
}
