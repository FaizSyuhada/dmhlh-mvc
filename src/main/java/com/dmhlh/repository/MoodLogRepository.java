package com.dmhlh.repository;

import com.dmhlh.entity.MoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLog, Long> {
    
    List<MoodLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<MoodLog> findTop7ByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<MoodLog> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime since);
    
    @Query("SELECT AVG(m.moodValue) FROM MoodLog m WHERE m.user.id = :userId AND m.createdAt >= :since")
    Double getAverageMoodSince(Long userId, LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM MoodLog m")
    long countTotal();
    
    @Query("SELECT COUNT(m) FROM MoodLog m WHERE m.createdAt >= :since")
    long countSince(LocalDateTime since);
}
