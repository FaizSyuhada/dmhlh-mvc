package com.dmhlh.repository;

import com.dmhlh.entity.CounsellorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounsellorNoteRepository extends JpaRepository<CounsellorNote, Long> {
    
    List<CounsellorNote> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
    
    List<CounsellorNote> findByAppointmentIdAndShareWithStudentTrueOrderByCreatedAtDesc(Long appointmentId);
}
