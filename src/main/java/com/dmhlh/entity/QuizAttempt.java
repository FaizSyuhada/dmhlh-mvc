package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private LearningModule module;
    
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;
    
    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;
    
    @Column(name = "score_percentage", nullable = false)
    private double scorePercentage;
    
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson; // JSON: {"questionId": "selectedOption", ...}
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }
}
