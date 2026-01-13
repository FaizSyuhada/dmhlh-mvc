package com.dmhlh.service;

import com.dmhlh.entity.Consent;
import com.dmhlh.entity.User;
import com.dmhlh.repository.ConsentRepository;
import com.dmhlh.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final ConsentRepository consentRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository,
                      ConsentRepository consentRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.consentRepository = consentRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> findByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> findCounsellors() {
        return userRepository.findByRoleAndEnabled(User.Role.COUNSELLOR, true);
    }
    
    @Transactional
    public User createUser(String email, String password, String displayName, User.Role role) {
        User user = User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .displayName(displayName)
            .role(role)
            .enabled(true)
            .build();
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User createOrGetMockUser(String email, String displayName, User.Role role) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> createUser(email, "password", displayName, role));
    }
    
    @Transactional
    public void acceptConsent(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Consent consent = consentRepository.findByUserId(userId)
            .orElseGet(() -> Consent.builder().user(user).build());
        
        consent.setAccepted(true);
        consent.setAcceptedAt(LocalDateTime.now());
        consent.setIpAddress(ipAddress);
        
        consentRepository.save(consent);
    }
    
    @Transactional
    public void declineConsent(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Consent consent = consentRepository.findByUserId(userId)
            .orElseGet(() -> Consent.builder().user(user).build());
        
        consent.setAccepted(false);
        consent.setIpAddress(ipAddress);
        
        consentRepository.save(consent);
    }
    
    public boolean hasAcceptedConsent(Long userId) {
        return consentRepository.existsByUserIdAndAcceptedTrue(userId);
    }
    
    public long countByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
    
    public List<Object[]> countUsersByRole() {
        return userRepository.countUsersByRole();
    }
    
    // Profile Management
    @Transactional
    public User updateProfile(Long userId, String displayName, String phoneNumber, 
                             String bio, String studentId, String faculty) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setDisplayName(displayName);
        user.setPhoneNumber(phoneNumber);
        user.setBio(bio);
        user.setStudentId(studentId);
        user.setFaculty(faculty);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void updateNotificationPreferences(Long userId, boolean emailNotifications, 
                                              boolean appointmentNotifications) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setNotificationEmail(emailNotifications);
        user.setNotificationAppointment(appointmentNotifications);
        
        userRepository.save(user);
    }
    
    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }
        
        // Update to new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
    
    // Consent Management
    public Optional<Consent> getConsent(Long userId) {
        return consentRepository.findByUserId(userId);
    }
    
    @Transactional
    public Consent updateConsentPreferences(Long userId, boolean moodTracking, boolean assessmentData,
                                           boolean appointmentHistory, boolean aiCoach,
                                           boolean anonymousAnalytics, boolean facultyReferral,
                                           String ipAddress) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Consent consent = consentRepository.findByUserId(userId)
            .orElseGet(() -> Consent.builder().user(user).build());
        
        consent.setConsentMoodTracking(moodTracking);
        consent.setConsentAssessmentData(assessmentData);
        consent.setConsentAppointmentHistory(appointmentHistory);
        consent.setConsentAiCoach(aiCoach);
        consent.setConsentAnonymousAnalytics(anonymousAnalytics);
        consent.setConsentFacultyReferral(facultyReferral);
        consent.setIpAddress(ipAddress);
        
        return consentRepository.save(consent);
    }
    
    @Transactional
    public void withdrawConsent(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Consent consent = consentRepository.findByUserId(userId)
            .orElseGet(() -> Consent.builder().user(user).build());
        
        consent.setAccepted(false);
        consent.setWithdrawnAt(LocalDateTime.now());
        consent.setIpAddress(ipAddress);
        
        // Reset all granular consents
        consent.setConsentMoodTracking(false);
        consent.setConsentAssessmentData(false);
        consent.setConsentAppointmentHistory(false);
        consent.setConsentAiCoach(false);
        consent.setConsentAnonymousAnalytics(false);
        consent.setConsentFacultyReferral(false);
        
        consentRepository.save(consent);
    }
}
