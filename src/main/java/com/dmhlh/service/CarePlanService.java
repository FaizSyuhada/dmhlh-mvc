package com.dmhlh.service;

import com.dmhlh.entity.AssessmentResult;
import com.dmhlh.entity.CarePlan;
import com.dmhlh.entity.User;
import com.dmhlh.repository.CarePlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CarePlanService {
    
    private final CarePlanRepository carePlanRepository;
    private final AssessmentService assessmentService;
    private final ObjectMapper objectMapper;
    
    public CarePlanService(CarePlanRepository carePlanRepository,
                          AssessmentService assessmentService) {
        this.carePlanRepository = carePlanRepository;
        this.assessmentService = assessmentService;
        this.objectMapper = new ObjectMapper();
    }
    
    public Optional<CarePlan> getLatestCarePlan(Long userId) {
        return carePlanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<CarePlan> getUserCarePlans(Long userId) {
        return carePlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Transactional
    public CarePlan generateCarePlan(Long userId) {
        Optional<AssessmentResult> latestResult = assessmentService.getLatestResult(userId);
        
        AssessmentResult.Severity riskLevel = latestResult
            .map(AssessmentResult::getSeverity)
            .orElse(AssessmentResult.Severity.MINIMAL);
        
        User user = new User();
        user.setId(userId);
        
        String summary = generateSummary(riskLevel);
        List<Map<String, String>> recommendations = generateRecommendations(riskLevel);
        
        String recommendationsJson;
        try {
            recommendationsJson = objectMapper.writeValueAsString(recommendations);
        } catch (JsonProcessingException e) {
            recommendationsJson = "[]";
        }
        
        CarePlan carePlan = CarePlan.builder()
            .user(user)
            .basedOnAssessment(latestResult.orElse(null))
            .riskLevel(riskLevel)
            .summary(summary)
            .recommendationsJson(recommendationsJson)
            .build();
        
        return carePlanRepository.save(carePlan);
    }
    
    private String generateSummary(AssessmentResult.Severity severity) {
        return switch (severity) {
            case MINIMAL -> 
                "Great news! Based on your recent assessment, you're doing well. Continue maintaining your positive mental health practices.";
            case MILD -> 
                "Your assessment indicates mild symptoms. This is a good time to focus on self-care and preventive strategies to maintain your wellbeing.";
            case MODERATE -> 
                "Your assessment shows moderate symptoms. Consider implementing the recommendations below and reaching out to a counsellor for additional support.";
            case MODERATELY_SEVERE -> 
                "Your assessment indicates moderately severe symptoms. We strongly encourage you to book an appointment with one of our counsellors. Support is available.";
            case SEVERE -> 
                "Your assessment shows severe symptoms. Please prioritize your mental health by booking a counselling session as soon as possible. You don't have to face this alone.";
        };
    }
    
    private List<Map<String, String>> generateRecommendations(AssessmentResult.Severity severity) {
        List<Map<String, String>> recommendations = new ArrayList<>();
        
        // Common recommendations for everyone
        recommendations.add(Map.of(
            "title", "Daily Mood Tracking",
            "description", "Log your mood daily to identify patterns and triggers.",
            "priority", "HIGH",
            "actionUrl", "/student/mood"
        ));
        
        switch (severity) {
            case MINIMAL -> {
                recommendations.add(Map.of(
                    "title", "Explore Learning Modules",
                    "description", "Browse our wellness modules to learn new strategies for maintaining mental health.",
                    "priority", "LOW",
                    "actionUrl", "/student/modules"
                ));
                recommendations.add(Map.of(
                    "title", "Stay Connected",
                    "description", "Maintain your social connections and continue activities you enjoy.",
                    "priority", "MEDIUM"
                ));
            }
            case MILD -> {
                recommendations.add(Map.of(
                    "title", "Complete Stress Management Module",
                    "description", "Our 'Managing Academic Stress' module has helpful coping strategies.",
                    "priority", "HIGH",
                    "actionUrl", "/student/modules"
                ));
                recommendations.add(Map.of(
                    "title", "Practice Breathing Exercises",
                    "description", "Try deep breathing exercises for 5 minutes each morning and before bed.",
                    "priority", "MEDIUM"
                ));
                recommendations.add(Map.of(
                    "title", "Maintain Sleep Routine",
                    "description", "Aim for 7-9 hours of sleep with a consistent schedule.",
                    "priority", "MEDIUM"
                ));
            }
            case MODERATE -> {
                recommendations.add(Map.of(
                    "title", "Consider Counselling",
                    "description", "Speaking with a counsellor can provide valuable support and strategies.",
                    "priority", "HIGH",
                    "actionUrl", "/student/appointments"
                ));
                recommendations.add(Map.of(
                    "title", "Complete Anxiety Module",
                    "description", "Learn about anxiety management in our 'Understanding Anxiety' module.",
                    "priority", "HIGH",
                    "actionUrl", "/student/modules"
                ));
                recommendations.add(Map.of(
                    "title", "Daily Self-Care",
                    "description", "Schedule at least 30 minutes daily for activities you enjoy.",
                    "priority", "MEDIUM"
                ));
                recommendations.add(Map.of(
                    "title", "Reach Out",
                    "description", "Talk to a trusted friend or family member about how you're feeling.",
                    "priority", "MEDIUM"
                ));
            }
            case MODERATELY_SEVERE, SEVERE -> {
                recommendations.add(Map.of(
                    "title", "Book Counselling Session",
                    "description", "Please prioritize booking an appointment with one of our counsellors.",
                    "priority", "URGENT",
                    "actionUrl", "/student/appointments"
                ));
                recommendations.add(Map.of(
                    "title", "Crisis Resources",
                    "description", "If you're in crisis, please reach out to emergency services or a crisis helpline.",
                    "priority", "URGENT"
                ));
                recommendations.add(Map.of(
                    "title", "Daily Check-ins",
                    "description", "Continue logging your mood daily so we can track your progress.",
                    "priority", "HIGH",
                    "actionUrl", "/student/mood"
                ));
                recommendations.add(Map.of(
                    "title", "Support Network",
                    "description", "Identify 2-3 people you can reach out to when you're struggling.",
                    "priority", "HIGH"
                ));
            }
        }
        
        return recommendations;
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> parseRecommendations(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
