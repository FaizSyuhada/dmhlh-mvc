package com.dmhlh.repository;

import com.dmhlh.entity.CounsellorProfile;
import com.dmhlh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounsellorProfileRepository extends JpaRepository<CounsellorProfile, Long> {
    Optional<CounsellorProfile> findByUser(User user);
    Optional<CounsellorProfile> findByUserId(Long userId);
    
    @Query("SELECT cp FROM CounsellorProfile cp JOIN FETCH cp.user WHERE cp.active = true ORDER BY cp.ratingAverage DESC")
    List<CounsellorProfile> findAllActiveWithUser();
    
    @Query("SELECT cp FROM CounsellorProfile cp JOIN FETCH cp.user WHERE cp.active = true AND cp.user.id = :userId")
    Optional<CounsellorProfile> findActiveByUserId(Long userId);
}
