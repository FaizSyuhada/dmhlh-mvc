package com.dmhlh.service;

import com.dmhlh.entity.Referral;
import com.dmhlh.entity.User;
import com.dmhlh.repository.ReferralRepository;
import com.dmhlh.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReferralService {
    
    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    
    public ReferralService(ReferralRepository referralRepository,
                          UserRepository userRepository) {
        this.referralRepository = referralRepository;
        this.userRepository = userRepository;
    }
    
    public List<Referral> getFacultyReferrals(Long facultyId) {
        return referralRepository.findByFacultyIdOrderByCreatedAtDesc(facultyId);
    }
    
    public List<Referral> getPendingReferrals() {
        return referralRepository.findByStatusInOrderByUrgencyDescCreatedAtDesc(
            List.of(Referral.Status.PENDING, Referral.Status.IN_REVIEW)
        );
    }
    
    public List<Referral> getCounsellorReferrals(Long counsellorId) {
        return referralRepository.findByAssignedCounsellorIdOrderByCreatedAtDesc(counsellorId);
    }
    
    public Optional<Referral> findById(Long id) {
        return referralRepository.findById(id);
    }
    
    @Transactional
    public Referral createReferral(Long facultyId, String studentIdentifier, 
                                   String summary, Referral.Urgency urgency, boolean consentGiven) {
        if (!consentGiven) {
            throw new IllegalArgumentException("Consent must be given to create a referral");
        }
        
        User faculty = userRepository.findById(facultyId)
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
        
        // Try to find the student
        User student = userRepository.findByEmail(studentIdentifier).orElse(null);
        
        Referral referral = Referral.builder()
            .faculty(faculty)
            .studentIdentifier(studentIdentifier)
            .student(student)
            .summary(summary)
            .urgency(urgency)
            .consentGiven(consentGiven)
            .status(Referral.Status.PENDING)
            .build();
        
        return referralRepository.save(referral);
    }
    
    @Transactional
    public Referral assignCounsellor(Long referralId, Long counsellorId) {
        Referral referral = referralRepository.findById(referralId)
            .orElseThrow(() -> new IllegalArgumentException("Referral not found"));
        User counsellor = userRepository.findById(counsellorId)
            .orElseThrow(() -> new IllegalArgumentException("Counsellor not found"));
        
        referral.setAssignedCounsellor(counsellor);
        referral.setStatus(Referral.Status.IN_REVIEW);
        
        return referralRepository.save(referral);
    }
    
    @Transactional
    public Referral updateStatus(Long referralId, Referral.Status status, String notes) {
        Referral referral = referralRepository.findById(referralId)
            .orElseThrow(() -> new IllegalArgumentException("Referral not found"));
        
        referral.setStatus(status);
        if (notes != null) {
            referral.setCounsellorNotes(notes);
        }
        
        return referralRepository.save(referral);
    }
    
    @Transactional
    public Referral addNotes(Long referralId, String notes) {
        Referral referral = referralRepository.findById(referralId)
            .orElseThrow(() -> new IllegalArgumentException("Referral not found"));
        
        String existingNotes = referral.getCounsellorNotes();
        String newNotes = existingNotes != null ? existingNotes + "\n\n" + notes : notes;
        referral.setCounsellorNotes(newNotes);
        
        return referralRepository.save(referral);
    }
    
    public long countPending() {
        return referralRepository.countByStatus(Referral.Status.PENDING);
    }
}
