package com.dmhlh.repository;

import com.dmhlh.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByStudentIdOrderByStartAtDesc(Long studentId);
    
    List<Appointment> findByCounsellorIdOrderByStartAtAsc(Long counsellorId);
    
    List<Appointment> findByCounsellorIdAndStatusOrderByStartAtAsc(Long counsellorId, Appointment.Status status);
    
    List<Appointment> findByStudentIdAndStatusInOrderByStartAtAsc(Long studentId, List<Appointment.Status> statuses);
    
    @Query("SELECT a FROM Appointment a WHERE a.counsellor.id = :counsellorId AND a.startAt >= :from ORDER BY a.startAt ASC")
    List<Appointment> findUpcomingByCounsellor(Long counsellorId, LocalDateTime from);
    
    @Query("SELECT a FROM Appointment a WHERE a.student.id = :studentId AND a.startAt >= :from ORDER BY a.startAt ASC")
    List<Appointment> findUpcomingByStudent(Long studentId, LocalDateTime from);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(Appointment.Status status);
    
    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countByStatusGrouped();
    
    long countByStudentIdAndStatus(Long studentId, Appointment.Status status);
}
