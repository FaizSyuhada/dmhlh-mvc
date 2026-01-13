package com.dmhlh.controller;

import com.dmhlh.entity.ForumPost;
import com.dmhlh.entity.ForumReport;
import com.dmhlh.entity.ForumThread;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.AuditLogService;
import com.dmhlh.service.ForumService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/moderation")
public class ModerationController {
    
    private final ForumService forumService;
    private final AuditLogService auditLogService;
    
    public ModerationController(ForumService forumService, AuditLogService auditLogService) {
        this.forumService = forumService;
        this.auditLogService = auditLogService;
    }
    
    @GetMapping
    public String moderation(Model model) {
        List<ForumReport> pendingReports = forumService.getPendingReports();
        List<ForumThread> pendingThreads = forumService.getPendingThreads();
        List<ForumPost> pendingPosts = forumService.getPendingPosts();
        
        model.addAttribute("reports", pendingReports);
        model.addAttribute("pendingThreads", pendingThreads);
        model.addAttribute("pendingPosts", pendingPosts);
        model.addAttribute("pendingModerationCount", pendingThreads.size() + pendingPosts.size());
        return "moderation/index";
    }
    
    @GetMapping("/report/{id}")
    public String reportDetail(@PathVariable Long id, Model model) {
        ForumReport report = forumService.findReportById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        model.addAttribute("report", report);
        return "moderation/report-detail";
    }
    
    @PostMapping("/report/{id}/resolve")
    public String resolveReport(@PathVariable Long id,
                               @RequestParam String action,
                               @RequestParam(required = false) String note,
                               @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes redirectAttributes) {
        forumService.resolveReport(id, user.getId(), action, note);
        
        auditLogService.logModerationAction(user.getId(), user.getEmail(), "ForumReport", id, action);
        
        redirectAttributes.addFlashAttribute("success", "Report resolved");
        return "redirect:/moderation";
    }
    
    @PostMapping("/report/{id}/dismiss")
    public String dismissReport(@PathVariable Long id,
                               @RequestParam(required = false) String note,
                               @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes redirectAttributes) {
        forumService.dismissReport(id, user.getId(), note);
        
        auditLogService.logModerationAction(user.getId(), user.getEmail(), "ForumReport", id, "DISMISS");
        
        redirectAttributes.addFlashAttribute("success", "Report dismissed");
        return "redirect:/moderation";
    }
    
    // ============= Pending Moderation Endpoints =============
    
    @PostMapping("/thread/{id}/approve")
    public String approveThread(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails user,
                               RedirectAttributes redirectAttributes) {
        forumService.approveThread(id);
        auditLogService.logModerationAction(user.getId(), user.getEmail(), "ForumThread", id, "APPROVE");
        redirectAttributes.addFlashAttribute("success", "Thread approved and now visible to users");
        return "redirect:/moderation";
    }
    
    @PostMapping("/thread/{id}/reject")
    public String rejectThread(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails user,
                              RedirectAttributes redirectAttributes) {
        forumService.rejectThread(id);
        auditLogService.logModerationAction(user.getId(), user.getEmail(), "ForumThread", id, "REJECT");
        redirectAttributes.addFlashAttribute("success", "Thread rejected and removed");
        return "redirect:/moderation";
    }
    
    @PostMapping("/post/{id}/approve")
    public String approvePost(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails user,
                             RedirectAttributes redirectAttributes) {
        forumService.approvePost(id);
        auditLogService.logModerationAction(user.getId(), user.getEmail(), "ForumPost", id, "APPROVE");
        redirectAttributes.addFlashAttribute("success", "Reply approved and now visible to users");
        return "redirect:/moderation";
    }
    
    @PostMapping("/post/{id}/reject")
    public String rejectPost(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails user,
                            RedirectAttributes redirectAttributes) {
        forumService.rejectPost(id);
        auditLogService.logModerationAction(user.getId(), user.getEmail(), "ForumPost", id, "REJECT");
        redirectAttributes.addFlashAttribute("success", "Reply rejected and removed");
        return "redirect:/moderation";
    }
}
