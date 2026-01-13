package com.dmhlh.dto;

import com.dmhlh.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardData {
    
    private AssessmentResult latestAssessment;
    private List<MoodLog> recentMoodLogs;
    private Double averageMood;
    private String moodTrend; // IMPROVING, STABLE, DECLINING, NEUTRAL
    private List<Nudge> activeNudges;
    private List<Appointment> upcomingAppointments;
    private CarePlan carePlan;
    private long availableModuleCount;
    
    // Gamification data
    private UserPoints userPoints;
    private int userRank;
    private List<UserBadge> userBadges;
    
    public String getMoodTrendIcon() {
        if (moodTrend == null) return "→";
        return switch (moodTrend) {
            case "IMPROVING" -> "↑";
            case "DECLINING" -> "↓";
            default -> "→";
        };
    }
    
    public String getMoodTrendClass() {
        if (moodTrend == null) return "text-secondary";
        return switch (moodTrend) {
            case "IMPROVING" -> "text-success";
            case "DECLINING" -> "text-danger";
            default -> "text-secondary";
        };
    }
    
    public boolean hasLowMood() {
        return averageMood != null && averageMood < 2.5;
    }
}
