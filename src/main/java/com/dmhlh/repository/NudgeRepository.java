package com.dmhlh.repository;

import com.dmhlh.entity.Nudge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NudgeRepository extends JpaRepository<Nudge, Long> {
    
    List<Nudge> findByUserIdAndDismissedAtIsNullOrderByCreatedAtDesc(Long userId);
    
    List<Nudge> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Nudge> findTop5ByUserIdAndDismissedAtIsNullOrderByCreatedAtDesc(Long userId);
}
