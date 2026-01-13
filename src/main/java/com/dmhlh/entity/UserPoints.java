package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPoints {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "total_points", nullable = false)
    private int totalPoints = 0;
    
    @Column(name = "current_level", nullable = false)
    private int currentLevel = 1;
    
    @Column(name = "xp_to_next_level", nullable = false)
    private int xpToNextLevel = 1000;
    
    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;
    
    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 0;
    
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
    
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
    
    public int getXpProgress() {
        int levelXp = totalPoints % xpToNextLevel;
        return (levelXp * 100) / xpToNextLevel;
    }
    
    public void addPoints(int points) {
        this.totalPoints += points;
        checkLevelUp();
    }
    
    private void checkLevelUp() {
        while (totalPoints >= currentLevel * 1000) {
            currentLevel++;
            xpToNextLevel = currentLevel * 1000;
        }
    }
}
