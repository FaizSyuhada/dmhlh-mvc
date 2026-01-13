package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean accepted = false;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    // Granular consent options
    @Column(name = "consent_mood_tracking", nullable = false)
    @Builder.Default
    private boolean consentMoodTracking = true;
    
    @Column(name = "consent_assessment_data", nullable = false)
    @Builder.Default
    private boolean consentAssessmentData = true;
    
    @Column(name = "consent_appointment_history", nullable = false)
    @Builder.Default
    private boolean consentAppointmentHistory = true;
    
    @Column(name = "consent_ai_coach", nullable = false)
    @Builder.Default
    private boolean consentAiCoach = true;
    
    @Column(name = "consent_anonymous_analytics", nullable = false)
    @Builder.Default
    private boolean consentAnonymousAnalytics = true;
    
    @Column(name = "consent_faculty_referral", nullable = false)
    @Builder.Default
    private boolean consentFacultyReferral = false;
    
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isWithdrawn() {
        return withdrawnAt != null;
    }
}
