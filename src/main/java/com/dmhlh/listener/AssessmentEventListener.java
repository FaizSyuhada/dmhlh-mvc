package com.dmhlh.listener;

import com.dmhlh.entity.AssessmentResult;
import com.dmhlh.entity.Nudge;
import com.dmhlh.service.CarePlanService;
import com.dmhlh.service.GamificationService;
import com.dmhlh.service.NudgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer pattern: listens for assessment events
 */
@Component
public class AssessmentEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(AssessmentEventListener.class);
    
    private final NudgeService nudgeService;
    private final CarePlanService carePlanService;
    private final GamificationService gamificationService;
    
    public AssessmentEventListener(NudgeService nudgeService, 
                                   CarePlanService carePlanService,
                                   GamificationService gamificationService) {
        this.nudgeService = nudgeService;
        this.carePlanService = carePlanService;
        this.gamificationService = gamificationService;
    }
    
    @Async
    @EventListener
    public void handleAssessmentSubmitted(AssessmentSubmittedEvent event) {
        AssessmentResult result = event.getResult();
        Long userId = result.getUser().getId();
        
        log.info("Processing assessment event for user {}: severity={}", userId, result.getSeverity());
        
        // Award points for completing assessment
        gamificationService.awardPointsForAssessment(result.getUser(), result.getId());
        log.info("Awarded points to user {} for assessment completion", userId);
        
        // Generate/update care plan
        carePlanService.generateCarePlan(userId);
        
        // Create nudges based on severity
        switch (result.getSeverity()) {
            case MODERATE -> nudgeService.createNudge(
                userId,
                NudgeService.NudgeTemplates.ASSESSMENT_MODERATE,
                Nudge.TriggerType.ASSESSMENT_RESULT,
                "/student/appointments",
                "Book Appointment"
            );
            case MODERATELY_SEVERE, SEVERE -> nudgeService.createNudge(
                userId,
                NudgeService.NudgeTemplates.ASSESSMENT_SEVERE,
                Nudge.TriggerType.ASSESSMENT_RESULT,
                "/student/appointments",
                "Book Urgent Appointment"
            );
            default -> {
                // No nudge for minimal/mild
            }
        }
    }
    
    // Event class
    public static class AssessmentSubmittedEvent {
        private final Object source;
        private final AssessmentResult result;
        
        public AssessmentSubmittedEvent(Object source, AssessmentResult result) {
            this.source = source;
            this.result = result;
        }
        
        public Object getSource() {
            return source;
        }
        
        public AssessmentResult getResult() {
            return result;
        }
    }
}
