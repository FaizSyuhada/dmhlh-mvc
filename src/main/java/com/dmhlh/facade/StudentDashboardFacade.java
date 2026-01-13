package com.dmhlh.facade;

import com.dmhlh.dto.StudentDashboardData;
import com.dmhlh.entity.*;
import com.dmhlh.service.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Facade pattern: aggregates data from multiple services
 * for the student dashboard view.
 */
@Component
public class StudentDashboardFacade {
    
    private final AssessmentService assessmentService;
    private final MoodLogService moodLogService;
    private final NudgeService nudgeService;
    private final AppointmentService appointmentService;
    private final CarePlanService carePlanService;
    private final LearningModuleService moduleService;
    private final GamificationService gamificationService;
    private final UserService userService;
    
    public StudentDashboardFacade(AssessmentService assessmentService,
                                  MoodLogService moodLogService,
                                  NudgeService nudgeService,
                                  AppointmentService appointmentService,
                                  CarePlanService carePlanService,
                                  LearningModuleService moduleService,
                                  GamificationService gamificationService,
                                  UserService userService) {
        this.assessmentService = assessmentService;
        this.moodLogService = moodLogService;
        this.nudgeService = nudgeService;
        this.appointmentService = appointmentService;
        this.carePlanService = carePlanService;
        this.moduleService = moduleService;
        this.gamificationService = gamificationService;
        this.userService = userService;
    }
    
    public StudentDashboardData getDashboardData(Long userId) {
        // Get latest assessment
        Optional<AssessmentResult> latestAssessment = assessmentService.getLatestResult(userId);
        
        // Get recent mood logs (last 7)
        List<MoodLog> recentMoods = moodLogService.getRecentLogs(userId, 7);
        
        // Calculate mood trend
        Optional<Double> averageMood7Days = moodLogService.getAverageMood(userId, 7);
        Optional<Double> averageMood14Days = moodLogService.getAverageMood(userId, 14);
        
        String moodTrend = calculateMoodTrend(averageMood7Days, averageMood14Days);
        
        // Get active nudges
        List<Nudge> activeNudges = nudgeService.getActiveNudges(userId);
        
        // Get upcoming appointments
        List<Appointment> upcomingAppointments = appointmentService.getUpcomingStudentAppointments(userId);
        
        // Get latest care plan
        Optional<CarePlan> carePlan = carePlanService.getLatestCarePlan(userId);
        
        // Get published modules count
        long moduleCount = moduleService.countPublished();
        
        // Get gamification data
        User user = userService.findById(userId).orElse(null);
        UserPoints userPoints = null;
        int userRank = 0;
        List<UserBadge> userBadges = List.of();
        
        if (user != null) {
            userPoints = gamificationService.getOrCreateUserPoints(user);
            userRank = gamificationService.getUserRank(user);
            userBadges = gamificationService.getUserBadges(user);
        }
        
        return StudentDashboardData.builder()
            .latestAssessment(latestAssessment.orElse(null))
            .recentMoodLogs(recentMoods)
            .averageMood(averageMood7Days.orElse(null))
            .moodTrend(moodTrend)
            .activeNudges(activeNudges)
            .upcomingAppointments(upcomingAppointments)
            .carePlan(carePlan.orElse(null))
            .availableModuleCount(moduleCount)
            .userPoints(userPoints)
            .userRank(userRank)
            .userBadges(userBadges)
            .build();
    }
    
    private String calculateMoodTrend(Optional<Double> recent, Optional<Double> earlier) {
        if (recent.isEmpty() || earlier.isEmpty()) {
            return "NEUTRAL";
        }
        
        double diff = recent.get() - earlier.get();
        
        if (diff > 0.3) {
            return "IMPROVING";
        } else if (diff < -0.3) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }
}
