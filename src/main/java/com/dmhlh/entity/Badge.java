package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "icon_url", length = 500)
    private String iconUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Column(name = "points_value", nullable = false)
    private int pointsValue = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false)
    private RequirementType requirementType;
    
    @Column(name = "requirement_value", nullable = false)
    private int requirementValue = 1;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum Category {
        LEARNING,
        ENGAGEMENT,
        WELLNESS,
        COMMUNITY,
        ACHIEVEMENT
    }
    
    public enum RequirementType {
        MODULES_COMPLETED,
        ASSESSMENTS_TAKEN,
        MOOD_STREAK,
        FORUM_POSTS,
        APPOINTMENTS_ATTENDED,
        POINTS_EARNED,
        CUSTOM
    }
}
