package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private LearningModule module;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "option_a", nullable = false)
    private String optionA;
    
    @Column(name = "option_b", nullable = false)
    private String optionB;
    
    @Column(name = "option_c", nullable = false)
    private String optionC;
    
    @Column(name = "option_d", nullable = false)
    private String optionD;
    
    @Column(name = "correct_option", nullable = false)
    private Character correctOption; // A, B, C, or D
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
