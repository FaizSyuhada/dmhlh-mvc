package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "counsellor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounsellorProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(length = 50)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String specializations;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "years_experience")
    private Integer yearsExperience;
    
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    @Column(name = "rating_average", precision = 3, scale = 2)
    private BigDecimal ratingAverage = BigDecimal.ZERO;
    
    @Column(name = "rating_count")
    private Integer ratingCount = 0;
    
    @Column(name = "max_daily_appointments")
    private Integer maxDailyAppointments = 8;
    
    @Column(name = "appointment_duration_minutes")
    private Integer appointmentDurationMinutes = 50;
    
    @Column(name = "buffer_minutes")
    private Integer bufferMinutes = 10;
    
    @Column(nullable = false)
    private boolean active = true;
    
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
    
    public List<String> getSpecializationsList() {
        if (specializations == null || specializations.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(specializations.split(",\\s*"));
    }
    
    public String getFullTitle() {
        String displayName = user.getDisplayName();
        if (title != null && !title.isEmpty()) {
            // Avoid duplicating title if displayName already starts with it
            if (displayName.startsWith(title)) {
                return displayName;
            }
            return title + " " + displayName;
        }
        return displayName;
    }
    
    public void updateRating(int newRating) {
        BigDecimal total = ratingAverage.multiply(BigDecimal.valueOf(ratingCount));
        ratingCount++;
        ratingAverage = total.add(BigDecimal.valueOf(newRating))
                             .divide(BigDecimal.valueOf(ratingCount), 2, java.math.RoundingMode.HALF_UP);
    }
}
