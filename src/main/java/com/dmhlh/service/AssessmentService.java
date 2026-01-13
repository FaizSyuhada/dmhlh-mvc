package com.dmhlh.service;

import com.dmhlh.entity.AssessmentDefinition;
import com.dmhlh.entity.AssessmentQuestion;
import com.dmhlh.entity.AssessmentResult;
import com.dmhlh.entity.User;
import com.dmhlh.repository.AssessmentDefinitionRepository;
import com.dmhlh.repository.AssessmentQuestionRepository;
import com.dmhlh.repository.AssessmentResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssessmentService {
    
    private final AssessmentDefinitionRepository definitionRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentResultRepository resultRepository;
    private final ObjectMapper objectMapper;
    
    public AssessmentService(AssessmentDefinitionRepository definitionRepository,
                            AssessmentQuestionRepository questionRepository,
                            AssessmentResultRepository resultRepository) {
        this.definitionRepository = definitionRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.objectMapper = new ObjectMapper();
    }
    
    public List<AssessmentDefinition> getActiveAssessments() {
        return definitionRepository.findByActiveTrue();
    }
    
    public Optional<AssessmentDefinition> findByCode(String code) {
        return definitionRepository.findByCode(code);
    }
    
    public Optional<AssessmentDefinition> findById(Long id) {
        return definitionRepository.findById(id);
    }
    
    public List<AssessmentQuestion> getQuestions(Long definitionId) {
        return questionRepository.findByDefinitionIdOrderByDisplayOrderAsc(definitionId);
    }
    
    public Optional<AssessmentResult> getLatestResult(Long userId) {
        return resultRepository.findFirstByUserIdOrderByCompletedAtDesc(userId);
    }
    
    public Optional<AssessmentResult> getLatestResult(Long userId, Long definitionId) {
        return resultRepository.findFirstByUserIdAndDefinitionIdOrderByCompletedAtDesc(userId, definitionId);
    }
    
    public Optional<AssessmentResult> findResultById(Long id) {
        return resultRepository.findById(id);
    }
    
    public List<AssessmentResult> getUserResults(Long userId) {
        return resultRepository.findByUserIdOrderByCompletedAtDesc(userId);
    }
    
    public List<AssessmentResult> getRecentResults(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return resultRepository.findByUserIdAndCompletedAtAfterOrderByCompletedAtDesc(userId, since);
    }
    
    @Transactional
    public AssessmentResult submitAssessment(Long userId, Long definitionId, Map<Long, Integer> responses) {
        User user = new User();
        user.setId(userId);
        
        AssessmentDefinition definition = definitionRepository.findById(definitionId)
            .orElseThrow(() -> new IllegalArgumentException("Assessment not found"));
        
        // Validate all questions are answered
        List<AssessmentQuestion> questions = getQuestions(definitionId);
        if (responses.size() < questions.size()) {
            throw new IllegalArgumentException("Please answer all questions before submitting.");
        }
        
        // Calculate total score
        int totalScore = responses.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        
        // Determine severity based on assessment type
        AssessmentResult.Severity severity = calculateSeverity(definition.getCode(), totalScore);
        
        String responsesJson;
        try {
            responsesJson = objectMapper.writeValueAsString(responses);
        } catch (JsonProcessingException e) {
            responsesJson = "{}";
        }
        
        AssessmentResult result = AssessmentResult.builder()
            .user(user)
            .definition(definition)
            .totalScore(totalScore)
            .severity(severity)
            .responsesJson(responsesJson)
            .build();
        
        return resultRepository.save(result);
    }
    
    /**
     * Calculate severity based on assessment type
     */
    public AssessmentResult.Severity calculateSeverity(String assessmentCode, int score) {
        return switch (assessmentCode) {
            case "PHQ9" -> calculatePHQ9Severity(score);
            case "GAD7" -> calculateGAD7Severity(score);
            default -> calculatePHQ9Severity(score); // Default to PHQ-9 scoring
        };
    }
    
    /**
     * PHQ-9 Depression Severity Scoring:
     * 0-4: Minimal
     * 5-9: Mild
     * 10-14: Moderate
     * 15-19: Moderately Severe
     * 20-27: Severe
     */
    public AssessmentResult.Severity calculatePHQ9Severity(int score) {
        if (score <= 4) {
            return AssessmentResult.Severity.MINIMAL;
        } else if (score <= 9) {
            return AssessmentResult.Severity.MILD;
        } else if (score <= 14) {
            return AssessmentResult.Severity.MODERATE;
        } else if (score <= 19) {
            return AssessmentResult.Severity.MODERATELY_SEVERE;
        } else {
            return AssessmentResult.Severity.SEVERE;
        }
    }
    
    /**
     * GAD-7 Anxiety Severity Scoring:
     * 0-4: Minimal anxiety
     * 5-9: Mild anxiety
     * 10-14: Moderate anxiety
     * 15-21: Severe anxiety
     */
    public AssessmentResult.Severity calculateGAD7Severity(int score) {
        if (score <= 4) {
            return AssessmentResult.Severity.MINIMAL;
        } else if (score <= 9) {
            return AssessmentResult.Severity.MILD;
        } else if (score <= 14) {
            return AssessmentResult.Severity.MODERATE;
        } else {
            return AssessmentResult.Severity.SEVERE;
        }
    }
    
    /**
     * Get feedback message based on assessment type and severity
     */
    public String getFeedbackMessage(String assessmentCode, AssessmentResult.Severity severity) {
        boolean isAnxiety = "GAD7".equals(assessmentCode);
        String condition = isAnxiety ? "anxiety" : "depression";
        
        return switch (severity) {
            case MINIMAL -> String.format(
                "✅ Your score suggests minimal %s. Continue practicing self-care and maintaining healthy habits. " +
                "Regular check-ins with yourself are a great way to stay mindful of your wellbeing.", condition);
            case MILD -> String.format(
                "💡 Your score suggests mild %s symptoms. Consider exploring our learning modules and " +
                "monitoring your mood regularly. Self-care strategies like exercise, sleep hygiene, and " +
                "mindfulness can be helpful. If symptoms persist, consider speaking with a counselor.", condition);
            case MODERATE -> String.format(
                "⚠️ Your score suggests moderate %s. We recommend booking an appointment with " +
                "one of our counselors to discuss supportive strategies. Professional guidance can help " +
                "you develop effective coping mechanisms.", condition);
            case MODERATELY_SEVERE -> String.format(
                "🔶 Your score suggests moderately severe %s. Please consider booking a counseling " +
                "appointment soon. Support is available and can make a significant difference. " +
                "You don't have to face this alone.", condition);
            case SEVERE -> String.format(
                "🆘 Your score suggests severe %s. We strongly encourage you to reach out for support. " +
                "Please book a counseling appointment or contact a crisis helpline if needed. " +
                "Remember: seeking help is a sign of strength.", condition);
        };
    }
    
    /**
     * Get recommendations based on assessment type and severity
     */
    public List<String> getRecommendations(String assessmentCode, AssessmentResult.Severity severity) {
        boolean isAnxiety = "GAD7".equals(assessmentCode);
        
        return switch (severity) {
            case MINIMAL -> List.of(
                "Continue your current self-care routine",
                "Practice regular mindfulness or meditation",
                "Maintain good sleep hygiene",
                "Check in with yourself periodically"
            );
            case MILD -> List.of(
                "Try our learning modules on " + (isAnxiety ? "managing anxiety" : "understanding depression"),
                "Practice deep breathing exercises daily",
                "Log your mood regularly to track patterns",
                "Engage in physical activity",
                "Consider talking to a friend or family member"
            );
            case MODERATE -> List.of(
                "Book a counseling appointment",
                "Start a mood journal",
                "Practice relaxation techniques",
                "Limit caffeine and alcohol",
                "Establish a consistent daily routine",
                "Use our AI Coach for additional support"
            );
            case MODERATELY_SEVERE, SEVERE -> List.of(
                "Book an urgent counseling appointment",
                "Talk to someone you trust about how you're feeling",
                "Use crisis resources if needed",
                "Try to maintain basic self-care (sleep, meals)",
                "Avoid alcohol and recreational drugs",
                "Remember: This is temporary and help is available"
            );
        };
    }
    
    public long countTotal() {
        return resultRepository.countTotal();
    }
    
    public long countSince(LocalDateTime since) {
        return resultRepository.countSince(since);
    }
}
