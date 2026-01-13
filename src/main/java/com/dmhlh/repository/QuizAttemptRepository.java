package com.dmhlh.repository;

import com.dmhlh.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    List<QuizAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);
    
    List<QuizAttempt> findByUserIdAndModuleId(Long userId, Long moduleId);
    
    Optional<QuizAttempt> findFirstByUserIdAndModuleIdOrderByCompletedAtDesc(Long userId, Long moduleId);
    
    @Query("SELECT COUNT(DISTINCT qa.module.id) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.scorePercentage >= 70")
    long countDistinctModulesByUserAndPassed(Long userId);
}
