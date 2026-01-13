package com.dmhlh.repository;

import com.dmhlh.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
    
    List<AssessmentResult> findByUserIdOrderByCompletedAtDesc(Long userId);
    
    Optional<AssessmentResult> findFirstByUserIdOrderByCompletedAtDesc(Long userId);
    
    Optional<AssessmentResult> findFirstByUserIdAndDefinitionIdOrderByCompletedAtDesc(Long userId, Long definitionId);
    
    @Query("SELECT COUNT(a) FROM AssessmentResult a")
    long countTotal();
    
    @Query("SELECT COUNT(a) FROM AssessmentResult a WHERE a.completedAt >= :since")
    long countSince(LocalDateTime since);
    
    List<AssessmentResult> findByUserIdAndCompletedAtAfterOrderByCompletedAtDesc(Long userId, LocalDateTime since);
    
    long countByUserId(Long userId);
}
