package com.dmhlh.repository;

import com.dmhlh.entity.User;
import com.dmhlh.entity.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {
    Optional<UserPoints> findByUser(User user);
    Optional<UserPoints> findByUserId(Long userId);
    
    @Query("SELECT up FROM UserPoints up JOIN FETCH up.user WHERE up.user.role = 'STUDENT' ORDER BY up.totalPoints DESC")
    List<UserPoints> findAllByOrderByTotalPointsDesc();
    
    @Query("SELECT up FROM UserPoints up JOIN FETCH up.user WHERE up.user.role = 'STUDENT' ORDER BY up.currentStreak DESC")
    List<UserPoints> findAllByOrderByCurrentStreakDesc();
    
    @Query(value = "SELECT COUNT(*) + 1 FROM user_points WHERE total_points > (SELECT total_points FROM user_points WHERE user_id = :userId)", nativeQuery = true)
    int findRankByUserId(Long userId);
}
