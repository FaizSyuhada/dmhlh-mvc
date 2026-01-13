package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "actor_id")
    private Long actorId;
    
    @Column(name = "actor_email")
    private String actorEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;
    
    @Column(name = "entity_type")
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum ActionType {
        LOGIN,
        LOGOUT,
        CONSENT_ACCEPTED,
        CONSENT_DECLINED,
        CONSENT_UPDATED,
        CONSENT_WITHDRAWN,
        PROFILE_UPDATED,
        PASSWORD_CHANGED,
        ASSESSMENT_SUBMITTED,
        MOOD_LOGGED,
        APPOINTMENT_CREATED,
        APPOINTMENT_CANCELLED,
        APPOINTMENT_COMPLETED,
        POST_CREATED,
        POST_REPORTED,
        MODERATION_ACTION,
        REFERRAL_CREATED,
        REFERRAL_STATUS_CHANGED,
        MODULE_CREATED,
        MODULE_UPDATED,
        SETTINGS_CHANGED
    }
}
