package com.dmhlh.repository;

import com.dmhlh.entity.PointTransaction;
import com.dmhlh.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);
    Page<PointTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'EARNED'")
    Integer sumPointsEarnedByUser(User user);
    
    List<PointTransaction> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime after);
}
