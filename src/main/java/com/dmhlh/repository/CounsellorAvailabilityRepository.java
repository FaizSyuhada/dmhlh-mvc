package com.dmhlh.repository;

import com.dmhlh.entity.CounsellorAvailability;
import com.dmhlh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CounsellorAvailabilityRepository extends JpaRepository<CounsellorAvailability, Long> {
    List<CounsellorAvailability> findByCounsellor(User counsellor);
    List<CounsellorAvailability> findByCounsellorId(Long counsellorId);
    
    @Query("SELECT ca FROM CounsellorAvailability ca WHERE ca.counsellor.id = :counsellorId AND ca.recurring = true AND ca.available = true")
    List<CounsellorAvailability> findRecurringByCounsellorId(Long counsellorId);
    
    @Query("SELECT ca FROM CounsellorAvailability ca WHERE ca.counsellor.id = :counsellorId AND ca.specificDate = :date AND ca.available = true")
    List<CounsellorAvailability> findBySpecificDate(Long counsellorId, LocalDate date);
    
    @Query("SELECT ca FROM CounsellorAvailability ca WHERE ca.counsellor.id = :counsellorId AND ca.dayOfWeek = :dayOfWeek AND ca.recurring = true AND ca.available = true")
    List<CounsellorAvailability> findByDayOfWeek(Long counsellorId, int dayOfWeek);
    
    void deleteByCounsellorIdAndRecurringTrue(Long counsellorId);
}
