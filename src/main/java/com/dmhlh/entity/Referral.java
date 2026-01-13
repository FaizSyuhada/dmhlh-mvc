package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty;
    
    @Column(name = "student_identifier", nullable = false)
    private String studentIdentifier; // Email or student ID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student; // Linked if user exists
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgency urgency;
    
    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_counsellor_id")
    private User assignedCounsellor;
    
    @Column(name = "counsellor_notes", columnDefinition = "TEXT")
    private String counsellorNotes;
    
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
    
    public enum Urgency {
        LOW,
        MEDIUM,
        HIGH
    }
    
    public enum Status {
        PENDING,
        IN_REVIEW,
        CLOSED
    }
}
