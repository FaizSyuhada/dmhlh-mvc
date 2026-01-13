package com.dmhlh.repository;

import com.dmhlh.entity.Consent;
import com.dmhlh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {
    
    Optional<Consent> findByUser(User user);
    
    Optional<Consent> findByUserId(Long userId);
    
    boolean existsByUserIdAndAcceptedTrue(Long userId);
}
