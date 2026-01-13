package com.dmhlh.controller;

import com.dmhlh.entity.Referral;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.AuditLogService;
import com.dmhlh.service.ReferralService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/faculty")
public class FacultyController {
    
    private final ReferralService referralService;
    private final AuditLogService auditLogService;
    
    public FacultyController(ReferralService referralService,
                            AuditLogService auditLogService) {
        this.referralService = referralService;
        this.auditLogService = auditLogService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        List<Referral> myReferrals = referralService.getFacultyReferrals(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("referrals", myReferrals);
        return "faculty/dashboard";
    }
    
    @GetMapping("/referrals/new")
    public String newReferralForm(Model model) {
        model.addAttribute("urgencies", Referral.Urgency.values());
        return "faculty/referral-form";
    }
    
    @PostMapping("/referrals/create")
    public String createReferral(@RequestParam String studentIdentifier,
                                @RequestParam String summary,
                                @RequestParam String urgency,
                                @RequestParam(defaultValue = "false") boolean consentGiven,
                                @AuthenticationPrincipal CustomUserDetails user,
                                RedirectAttributes redirectAttributes) {
        if (!consentGiven) {
            redirectAttributes.addFlashAttribute("error", "You must confirm consent has been obtained");
            return "redirect:/faculty/referrals/new";
        }
        
        Referral.Urgency urgencyLevel = Referral.Urgency.valueOf(urgency);
        
        Referral referral = referralService.createReferral(
            user.getId(), studentIdentifier, summary, urgencyLevel, consentGiven);
        
        auditLogService.logReferralCreated(user.getId(), user.getEmail(), referral.getId());
        
        redirectAttributes.addFlashAttribute("success", "Referral submitted successfully");
        return "redirect:/faculty/dashboard";
    }
}
