package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "student_id", length = 50)
    private String studentId;
    
    @Column(length = 100)
    private String faculty;
    
    @Column(name = "notification_email", nullable = false)
    @Builder.Default
    private boolean notificationEmail = true;
    
    @Column(name = "notification_appointment", nullable = false)
    @Builder.Default
    private boolean notificationAppointment = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
    
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
    
    /**
     * Get initials from the display name, skipping titles like Dr., Prof., etc.
     */
    public String getInitials() {
        if (displayName == null || displayName.isEmpty()) {
            return "?";
        }
        
        // Remove common titles
        String name = displayName
            .replaceFirst("(?i)^(Dr\\.?|Prof\\.?|Mr\\.?|Ms\\.?|Mrs\\.?)\\s+", "")
            .trim();
        
        String[] parts = name.split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        } else if (parts.length == 1 && !parts[0].isEmpty()) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return "?";
    }
    
    public enum Role {
        STUDENT,
        COUNSELLOR,
        FACULTY,
        ADMIN
    }
}
