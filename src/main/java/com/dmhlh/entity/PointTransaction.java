package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private int points;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;
    
    @Column(name = "source_id")
    private Long sourceId;
    
    @Column(length = 255)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum TransactionType {
        EARNED,
        SPENT,
        BONUS,
        BADGE_REWARD
    }
    
    public enum Source {
        MOOD_LOG,
        ASSESSMENT,
        MODULE_COMPLETE,
        QUIZ_PASS,
        FORUM_POST,
        FORUM_HELPFUL,
        APPOINTMENT,
        STREAK_BONUS,
        BADGE,
        ADMIN
    }
}
