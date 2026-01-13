package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nudges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nudge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;
    
    @Column(name = "action_url")
    private String actionUrl;
    
    @Column(name = "action_label")
    private String actionLabel;
    
    @Column(name = "seen_at")
    private LocalDateTime seenAt;
    
    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum TriggerType {
        LOW_MOOD_STREAK,
        ASSESSMENT_DUE,
        ASSESSMENT_RESULT,
        APPOINTMENT_REMINDER,
        LEARNING_SUGGESTION,
        GENERAL_WELLNESS
    }
}
