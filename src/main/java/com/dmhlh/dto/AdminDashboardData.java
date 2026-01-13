package com.dmhlh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardData {
    
    private long studentCount;
    private long counsellorCount;
    private long facultyCount;
    private long adminCount;
    
    private long totalAssessments;
    private long assessmentsLast7Days;
    
    private long totalMoodLogs;
    private long moodLogsLast7Days;
    
    private long openReports;
    
    private long scheduledAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    
    private long pendingReferrals;
    
    // Alias methods for template compatibility
    public long getTotalUsers() {
        return studentCount + counsellorCount + facultyCount + adminCount;
    }
    
    public long getTotalStudents() {
        return studentCount;
    }
    
    public long getTotalCounsellors() {
        return counsellorCount;
    }
    
    public long getTotalFaculty() {
        return facultyCount;
    }
    
    public long getTotalAdmins() {
        return adminCount;
    }
    
    public long getTotalAppointments() {
        return scheduledAppointments + completedAppointments + cancelledAppointments;
    }
}
