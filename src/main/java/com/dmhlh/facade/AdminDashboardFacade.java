package com.dmhlh.facade;

import com.dmhlh.dto.AdminDashboardData;
import com.dmhlh.entity.Appointment;
import com.dmhlh.entity.User;
import com.dmhlh.service.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Facade pattern: aggregates KPI data for admin dashboard
 */
@Component
public class AdminDashboardFacade {
    
    private final UserService userService;
    private final AssessmentService assessmentService;
    private final MoodLogService moodLogService;
    private final ForumService forumService;
    private final AppointmentService appointmentService;
    private final ReferralService referralService;
    
    public AdminDashboardFacade(UserService userService,
                                AssessmentService assessmentService,
                                MoodLogService moodLogService,
                                ForumService forumService,
                                AppointmentService appointmentService,
                                ReferralService referralService) {
        this.userService = userService;
        this.assessmentService = assessmentService;
        this.moodLogService = moodLogService;
        this.forumService = forumService;
        this.appointmentService = appointmentService;
        this.referralService = referralService;
    }
    
    public AdminDashboardData getDashboardData() {
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        
        // User counts by role
        long studentCount = userService.countByRole(User.Role.STUDENT);
        long counsellorCount = userService.countByRole(User.Role.COUNSELLOR);
        long facultyCount = userService.countByRole(User.Role.FACULTY);
        long adminCount = userService.countByRole(User.Role.ADMIN);
        
        // Assessment stats
        long totalAssessments = assessmentService.countTotal();
        long assessmentsLast7Days = assessmentService.countSince(last7Days);
        
        // Mood log stats
        long totalMoodLogs = moodLogService.countTotal();
        long moodLogsLast7Days = moodLogService.countSince(last7Days);
        
        // Forum stats
        long openReports = forumService.countOpenReports();
        
        // Appointment stats
        Map<Appointment.Status, Long> appointmentsByStatus = appointmentService.getAppointmentCountsByStatus();
        long scheduledAppointments = appointmentsByStatus.getOrDefault(Appointment.Status.SCHEDULED, 0L);
        long completedAppointments = appointmentsByStatus.getOrDefault(Appointment.Status.COMPLETED, 0L);
        long cancelledAppointments = appointmentsByStatus.getOrDefault(Appointment.Status.CANCELLED, 0L);
        
        // Referral stats
        long pendingReferrals = referralService.countPending();
        
        return AdminDashboardData.builder()
            .studentCount(studentCount)
            .counsellorCount(counsellorCount)
            .facultyCount(facultyCount)
            .adminCount(adminCount)
            .totalAssessments(totalAssessments)
            .assessmentsLast7Days(assessmentsLast7Days)
            .totalMoodLogs(totalMoodLogs)
            .moodLogsLast7Days(moodLogsLast7Days)
            .openReports(openReports)
            .scheduledAppointments(scheduledAppointments)
            .completedAppointments(completedAppointments)
            .cancelledAppointments(cancelledAppointments)
            .pendingReferrals(pendingReferrals)
            .build();
    }
}
