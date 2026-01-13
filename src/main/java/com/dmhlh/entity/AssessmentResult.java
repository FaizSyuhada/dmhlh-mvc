package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private AssessmentDefinition definition;
    
    @Column(name = "total_score", nullable = false)
    private int totalScore;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    
    @Column(name = "responses_json", nullable = false, columnDefinition = "TEXT")
    private String responsesJson; // JSON: {"questionId": score, ...}
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }
    
    public enum Severity {
        MINIMAL,
        MILD,
        MODERATE,
        MODERATELY_SEVERE,
        SEVERE
    }
}
