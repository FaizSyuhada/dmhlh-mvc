package com.dmhlh.listener;

import com.dmhlh.entity.MoodLog;
import com.dmhlh.entity.Nudge;
import com.dmhlh.entity.User;
import com.dmhlh.service.GamificationService;
import com.dmhlh.service.MoodLogService;
import com.dmhlh.service.NudgeService;
import com.dmhlh.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer pattern implementation: listens for domain events
 * and triggers nudge creation based on rules.
 */
@Component
public class MoodLogEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(MoodLogEventListener.class);
    
    private final MoodLogService moodLogService;
    private final NudgeService nudgeService;
    private final GamificationService gamificationService;
    private final UserService userService;
    
    private static final int LOW_MOOD_THRESHOLD = 2;
    private static final int STREAK_SIZE = 3;
    
    public MoodLogEventListener(MoodLogService moodLogService, 
                                NudgeService nudgeService,
                                GamificationService gamificationService,
                                UserService userService) {
        this.moodLogService = moodLogService;
        this.nudgeService = nudgeService;
        this.gamificationService = gamificationService;
        this.userService = userService;
    }
    
    @Async
    @EventListener
    public void handleMoodLogged(MoodLogService.MoodLoggedEvent event) {
        MoodLog moodLog = event.getMoodLog();
        Long userId = moodLog.getUser().getId();
        
        // Award points for mood logging
        userService.findById(userId).ifPresent(user -> {
            gamificationService.awardPointsForMoodLog(user, moodLog.getId());
            log.info("Awarded points to user {} for mood log", userId);
        });
        
        log.debug("Processing mood log event for user {}: mood={}", userId, moodLog.getMoodValue());
        
        // Rule 1: Check for low mood streak
        if (moodLogService.hasLowMoodStreak(userId, STREAK_SIZE, LOW_MOOD_THRESHOLD)) {
            log.info("Low mood streak detected for user {}, creating nudge", userId);
            
            nudgeService.createNudge(
                userId,
                NudgeService.NudgeTemplates.LOW_MOOD_STREAK,
                Nudge.TriggerType.LOW_MOOD_STREAK,
                "/student/appointments",
                "Book Appointment"
            );
        }
        
        // Rule 2: If single mood is very low (1), suggest resources
        if (moodLog.getMoodValue() == 1) {
            nudgeService.createNudge(
                userId,
                "We noticed you're having a tough time. Remember, support is available. Consider exploring our learning modules or talking to someone.",
                Nudge.TriggerType.GENERAL_WELLNESS,
                "/student/ai-coach",
                "Talk to AI Coach"
            );
        }
    }
}
