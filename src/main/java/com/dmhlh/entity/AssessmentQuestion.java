package com.dmhlh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assessment_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private AssessmentDefinition definition;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
    
    // For PHQ-9/GAD-7: options are standard (0-3 scale)
    // Option text stored as JSON or we use standard scale
    @Column(name = "option_0_text")
    private String option0Text; // e.g., "Not at all"
    
    @Column(name = "option_1_text")
    private String option1Text; // e.g., "Several days"
    
    @Column(name = "option_2_text")
    private String option2Text; // e.g., "More than half the days"
    
    @Column(name = "option_3_text")
    private String option3Text; // e.g., "Nearly every day"
}
