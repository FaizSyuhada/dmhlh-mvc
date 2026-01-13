package com.dmhlh.controller;

import com.dmhlh.entity.*;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/counsellor")
public class CounsellorController {
    
    private final AppointmentService appointmentService;
    private final ReferralService referralService;
    private final AssessmentService assessmentService;
    private final MoodLogService moodLogService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    
    public CounsellorController(AppointmentService appointmentService,
                               ReferralService referralService,
                               AssessmentService assessmentService,
                               MoodLogService moodLogService,
                               UserService userService,
                               AuditLogService auditLogService) {
        this.appointmentService = appointmentService;
        this.referralService = referralService;
        this.assessmentService = assessmentService;
        this.moodLogService = moodLogService;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<Appointment> todaySchedule = appointmentService.getUpcomingCounsellorAppointments(user.getId());
        List<Referral> pendingReferralsList = referralService.getPendingReferrals();
        long totalStudents = userService.countByRole(User.Role.STUDENT);
        
        model.addAttribute("user", user);
        model.addAttribute("todayAppointments", todaySchedule.size());
        model.addAttribute("todaySchedule", todaySchedule);
        model.addAttribute("pendingReferrals", pendingReferralsList.size());
        model.addAttribute("pendingReports", 0); // TODO: Add actual report count from moderation service
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("currentPage", "dashboard");
        return "counsellor/dashboard";
    }
    
    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<Appointment> appointments = appointmentService.getCounsellorAppointments(user.getId());
        model.addAttribute("appointments", appointments);
        return "counsellor/appointments";
    }
    
    @GetMapping("/appointments/{id}")
    public String appointmentDetail(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   Model model) {
        Appointment appointment = appointmentService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        // Get student summary
        Long studentId = appointment.getStudent().getId();
        AssessmentResult latestAssessment = assessmentService.getLatestResult(studentId).orElse(null);
        List<MoodLog> recentMoods = moodLogService.getLogsSince(studentId, 14);
        Double averageMood = moodLogService.getAverageMood(studentId, 14).orElse(null);
        
        // Get notes
        List<CounsellorNote> notes = appointmentService.getNotes(id);
        
        model.addAttribute("appointment", appointment);
        model.addAttribute("latestAssessment", latestAssessment);
        model.addAttribute("recentMoods", recentMoods);
        model.addAttribute("averageMood", averageMood);
        model.addAttribute("notes", notes);
        return "counsellor/appointment-detail";
    }
    
    @PostMapping("/appointments/{id}/note")
    public String addNote(@PathVariable Long id,
                         @RequestParam String note,
                         @RequestParam(defaultValue = "false") boolean shareWithStudent,
                         @AuthenticationPrincipal CustomUserDetails user,
                         RedirectAttributes redirectAttributes) {
        appointmentService.addNote(id, user.getId(), note, shareWithStudent);
        
        redirectAttributes.addFlashAttribute("success", "Note added successfully");
        return "redirect:/counsellor/appointments/" + id;
    }
    
    @PostMapping("/appointments/{id}/status")
    public String updateStatus(@PathVariable Long id,
                              @RequestParam String status,
                              @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes) {
        Appointment.Status newStatus = Appointment.Status.valueOf(status);
        
        switch (newStatus) {
            case CONFIRMED -> appointmentService.confirm(id);
            case COMPLETED -> appointmentService.complete(id);
            case CANCELLED -> appointmentService.cancel(id, "Cancelled by counsellor");
            default -> throw new IllegalArgumentException("Invalid status");
        }
        
        redirectAttributes.addFlashAttribute("success", "Appointment status updated");
        return "redirect:/counsellor/appointments/" + id;
    }
    
    @GetMapping("/referrals")
    public String referrals(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<Referral> referrals = referralService.getPendingReferrals();
        List<Referral> myReferrals = referralService.getCounsellorReferrals(user.getId());
        
        model.addAttribute("pendingReferrals", referrals);
        model.addAttribute("myReferrals", myReferrals);
        return "counsellor/referrals";
    }
    
    @PostMapping("/referrals/{id}/assign")
    public String assignReferral(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails user,
                                RedirectAttributes redirectAttributes) {
        referralService.assignCounsellor(id, user.getId());
        
        auditLogService.logReferralStatusChanged(user.getId(), user.getEmail(), id, "IN_REVIEW");
        
        redirectAttributes.addFlashAttribute("success", "Referral assigned to you");
        return "redirect:/counsellor/referrals";
    }
    
    @PostMapping("/referrals/{id}/status")
    public String updateReferralStatus(@PathVariable Long id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String notes,
                                      @AuthenticationPrincipal CustomUserDetails user,
                                      RedirectAttributes redirectAttributes) {
        Referral.Status newStatus = Referral.Status.valueOf(status);
        referralService.updateStatus(id, newStatus, notes);
        
        auditLogService.logReferralStatusChanged(user.getId(), user.getEmail(), id, status);
        
        redirectAttributes.addFlashAttribute("success", "Referral status updated");
        return "redirect:/counsellor/referrals";
    }
}
