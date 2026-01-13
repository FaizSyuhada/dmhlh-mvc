package com.dmhlh.repository;

import com.dmhlh.entity.User;
import com.dmhlh.entity.UserBadge;
import com.dmhlh.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserOrderByEarnedAtDesc(User user);
    List<UserBadge> findByUserId(Long userId);
    Optional<UserBadge> findByUserAndBadge(User user, Badge badge);
    boolean existsByUserAndBadge(User user, Badge badge);
    long countByUser(User user);
    
    @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user.id = :userId ORDER BY ub.earnedAt DESC")
    List<UserBadge> findByUserIdWithBadge(Long userId);
}
