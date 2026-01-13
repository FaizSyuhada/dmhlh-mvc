package com.dmhlh.repository;

import com.dmhlh.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    
    List<Referral> findByFacultyIdOrderByCreatedAtDesc(Long facultyId);
    
    List<Referral> findByStatusOrderByUrgencyDescCreatedAtDesc(Referral.Status status);
    
    List<Referral> findByStatusInOrderByUrgencyDescCreatedAtDesc(List<Referral.Status> statuses);
    
    List<Referral> findByAssignedCounsellorIdOrderByCreatedAtDesc(Long counsellorId);
    
    long countByStatus(Referral.Status status);
}
