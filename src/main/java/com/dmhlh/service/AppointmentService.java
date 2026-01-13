package com.dmhlh.service;

import com.dmhlh.entity.Appointment;
import com.dmhlh.entity.CounsellorNote;
import com.dmhlh.entity.SessionType;
import com.dmhlh.entity.User;
import com.dmhlh.repository.AppointmentRepository;
import com.dmhlh.repository.CounsellorNoteRepository;
import com.dmhlh.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final CounsellorNoteRepository noteRepository;
    private final UserRepository userRepository;
    
    public AppointmentService(AppointmentRepository appointmentRepository,
                             CounsellorNoteRepository noteRepository,
                             UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }
    
    public List<Appointment> getStudentAppointments(Long studentId) {
        return appointmentRepository.findByStudentIdOrderByStartAtDesc(studentId);
    }
    
    public List<Appointment> getUpcomingStudentAppointments(Long studentId) {
        List<Appointment.Status> statuses = List.of(
            Appointment.Status.SCHEDULED,
            Appointment.Status.CONFIRMED
        );
        return appointmentRepository.findByStudentIdAndStatusInOrderByStartAtAsc(studentId, statuses);
    }
    
    public List<Appointment> getCounsellorAppointments(Long counsellorId) {
        return appointmentRepository.findByCounsellorIdOrderByStartAtAsc(counsellorId);
    }
    
    public List<Appointment> getUpcomingCounsellorAppointments(Long counsellorId) {
        return appointmentRepository.findUpcomingByCounsellor(counsellorId, LocalDateTime.now());
    }
    
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }
    
    @Transactional
    public Appointment bookAppointment(Long studentId, Long counsellorId,
                                       LocalDateTime startAt, String reason) {
        return bookAppointment(studentId, counsellorId, startAt, reason, null);
    }
    
    @Transactional
    public Appointment bookAppointment(Long studentId, Long counsellorId,
                                       LocalDateTime startAt, String reason, SessionType sessionType) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        User counsellor = userRepository.findById(counsellorId)
            .orElseThrow(() -> new IllegalArgumentException("Counsellor not found"));
        
        // Default duration: 1 hour
        LocalDateTime endAt = startAt.plusHours(1);
        
        Appointment.AppointmentBuilder builder = Appointment.builder()
            .student(student)
            .counsellor(counsellor)
            .startAt(startAt)
            .endAt(endAt)
            .status(Appointment.Status.SCHEDULED)
            .reason(reason);
        
        // Set session type if provided
        if (sessionType != null) {
            builder.sessionType(sessionType);
            // Generate meeting link for video calls
            if (sessionType.getName().toLowerCase().contains("video")) {
                // Meeting link will be set after save to include ID
            }
        }
        
        Appointment appointment = builder.build();
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Set meeting link after save to include appointment ID
        if (sessionType != null && sessionType.getName().toLowerCase().contains("video")) {
            savedAppointment.setMeetingLink("https://meet.solace.edu/session/" + savedAppointment.getId());
            savedAppointment = appointmentRepository.save(savedAppointment);
        }
        
        return savedAppointment;
    }
    
    @Transactional
    public Appointment reschedule(Long appointmentId, LocalDateTime newStartAt) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        appointment.setStartAt(newStartAt);
        appointment.setEndAt(newStartAt.plusHours(1));
        
        return appointmentRepository.save(appointment);
    }
    
    @Transactional
    public Appointment cancel(Long appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        appointment.setStatus(Appointment.Status.CANCELLED);
        appointment.setCancellationReason(reason);
        
        return appointmentRepository.save(appointment);
    }
    
    @Transactional
    public Appointment complete(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        appointment.setStatus(Appointment.Status.COMPLETED);
        
        return appointmentRepository.save(appointment);
    }
    
    @Transactional
    public Appointment confirm(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        appointment.setStatus(Appointment.Status.CONFIRMED);
        
        return appointmentRepository.save(appointment);
    }
    
    @Transactional
    public CounsellorNote addNote(Long appointmentId, Long authorId, String noteText, boolean shareWithStudent) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        
        CounsellorNote note = CounsellorNote.builder()
            .appointment(appointment)
            .author(author)
            .note(noteText)
            .shareWithStudent(shareWithStudent)
            .build();
        
        return noteRepository.save(note);
    }
    
    public List<CounsellorNote> getNotes(Long appointmentId) {
        return noteRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }
    
    public List<CounsellorNote> getSharedNotes(Long appointmentId) {
        return noteRepository.findByAppointmentIdAndShareWithStudentTrueOrderByCreatedAtDesc(appointmentId);
    }
    
    public Map<Appointment.Status, Long> getAppointmentCountsByStatus() {
        return appointmentRepository.countByStatusGrouped().stream()
            .collect(Collectors.toMap(
                arr -> (Appointment.Status) arr[0],
                arr -> (Long) arr[1]
            ));
    }
    
    public long countByStatus(Appointment.Status status) {
        return appointmentRepository.countByStatus(status);
    }
}
