package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mood_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "mood_value", nullable = false)
    private int moodValue; // 1-5 scale
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public String getMoodEmoji() {
        return switch (moodValue) {
            case 1 -> "😢";
            case 2 -> "😔";
            case 3 -> "😐";
            case 4 -> "🙂";
            case 5 -> "😊";
            default -> "❓";
        };
    }
    
    public String getMoodLabel() {
        return switch (moodValue) {
            case 1 -> "Very Low";
            case 2 -> "Low";
            case 3 -> "Neutral";
            case 4 -> "Good";
            case 5 -> "Great";
            default -> "Unknown";
        };
    }
}
