package com.dmhlh.controller;

import com.dmhlh.entity.Consent;
import com.dmhlh.entity.User;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.AuditLogService;
import com.dmhlh.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    private final AuditLogService auditLogService;
    
    @GetMapping
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Consent consent = userService.getConsent(userDetails.getId()).orElse(null);
        
        model.addAttribute("user", user);
        model.addAttribute("consent", consent);
        model.addAttribute("activeTab", "profile");
        
        return "profile/index";
    }
    
    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "settings");
        
        return "profile/index";
    }
    
    @GetMapping("/consent")
    public String consent(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Consent consent = userService.getConsent(userDetails.getId()).orElse(null);
        
        model.addAttribute("user", user);
        model.addAttribute("consent", consent);
        model.addAttribute("activeTab", "consent");
        
        return "profile/index";
    }
    
    @PostMapping("/update")
    public String updateProfile(@RequestParam String displayName,
                               @RequestParam(required = false) String phoneNumber,
                               @RequestParam(required = false) String bio,
                               @RequestParam(required = false) String studentId,
                               @RequestParam(required = false) String faculty,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        userService.updateProfile(userDetails.getId(), displayName, phoneNumber, bio, studentId, faculty);
        
        auditLogService.logProfileUpdated(userDetails.getId(), userDetails.getEmail());
        
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }
    
    @PostMapping("/notifications")
    public String updateNotifications(@RequestParam(defaultValue = "false") boolean emailNotifications,
                                     @RequestParam(defaultValue = "false") boolean appointmentNotifications,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        
        userService.updateNotificationPreferences(userDetails.getId(), emailNotifications, appointmentNotifications);
        
        redirectAttributes.addFlashAttribute("success", "Notification preferences saved!");
        return "redirect:/profile/settings";
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        
        // Validate new password
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile/settings";
        }
        
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters");
            return "redirect:/profile/settings";
        }
        
        boolean success = userService.changePassword(userDetails.getId(), currentPassword, newPassword);
        
        if (success) {
            auditLogService.logPasswordChanged(userDetails.getId(), userDetails.getEmail());
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
        }
        
        return "redirect:/profile/settings";
    }
    
    @PostMapping("/consent/update")
    public String updateConsent(@RequestParam(defaultValue = "false") boolean moodTracking,
                               @RequestParam(defaultValue = "false") boolean assessmentData,
                               @RequestParam(defaultValue = "false") boolean appointmentHistory,
                               @RequestParam(defaultValue = "false") boolean aiCoach,
                               @RequestParam(defaultValue = "false") boolean anonymousAnalytics,
                               @RequestParam(defaultValue = "false") boolean facultyReferral,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        
        String ipAddress = request.getRemoteAddr();
        
        userService.updateConsentPreferences(userDetails.getId(), moodTracking, assessmentData,
            appointmentHistory, aiCoach, anonymousAnalytics, facultyReferral, ipAddress);
        
        auditLogService.logConsentUpdated(userDetails.getId(), userDetails.getEmail());
        
        redirectAttributes.addFlashAttribute("success", "Consent preferences saved!");
        return "redirect:/profile/consent";
    }
    
    @PostMapping("/consent/withdraw")
    public String withdrawConsent(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        
        String ipAddress = request.getRemoteAddr();
        
        userService.withdrawConsent(userDetails.getId(), ipAddress);
        
        auditLogService.logConsentWithdrawn(userDetails.getId(), userDetails.getEmail());
        
        redirectAttributes.addFlashAttribute("warning", "Your consent has been withdrawn. Some features may no longer be available.");
        return "redirect:/profile/consent";
    }
}
