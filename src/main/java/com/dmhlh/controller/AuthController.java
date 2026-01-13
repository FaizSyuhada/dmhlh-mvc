package com.dmhlh.controller;

import com.dmhlh.entity.User;
import com.dmhlh.security.CustomUserDetails;
import com.dmhlh.service.AuditLogService;
import com.dmhlh.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final AuthenticationManager authenticationManager;
    
    public AuthController(UserService userService,
                         AuditLogService auditLogService,
                         AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.authenticationManager = authenticationManager;
    }
    
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                       @RequestParam(required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }
    
    @GetMapping("/consent")
    public String consentPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            if (userService.hasAcceptedConsent(user.getId())) {
                return "redirect:" + getRedirectUrlForRole(user.getRole());
            }
        }
        return "auth/consent";
    }
    
    @PostMapping("/consent/accept")
    public String acceptConsent(HttpServletRequest request, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        userService.acceptConsent(user.getId(), request.getRemoteAddr());
        auditLogService.logConsentAccepted(user.getId(), user.getEmail(), request.getRemoteAddr());
        return "redirect:" + getRedirectUrlForRole(user.getRole());
    }
    
    @PostMapping("/consent/decline")
    public String declineConsent(HttpServletRequest request, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        userService.declineConsent(user.getId(), request.getRemoteAddr());
        auditLogService.logConsentDeclined(user.getId(), user.getEmail(), request.getRemoteAddr());
        // Redirect to limited access - only learning modules
        return "redirect:/student/modules";
    }
    
    /**
     * Mock login endpoint for demo purposes.
     * Allows quick login with predefined demo users.
     */
    @GetMapping("/mock-login/{role}")
    public String mockLogin(@PathVariable String role, HttpServletRequest request) {
        String email = switch (role.toUpperCase()) {
            case "ADMIN" -> "admin@dmhlh.test";
            case "COUNSELLOR" -> "counsellor@dmhlh.test";
            case "FACULTY" -> "faculty@dmhlh.test";
            case "STUDENT1" -> "student1@dmhlh.test";
            case "STUDENT2" -> "student2@dmhlh.test";
            default -> "student1@dmhlh.test";
        };
        
        try {
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(email, "password");
            Authentication auth = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
            auditLogService.logLogin(user.getId(), user.getEmail(), 
                                    request.getRemoteAddr(), request.getHeader("User-Agent"));
            
            // Check consent
            if ((user.getRole() == User.Role.STUDENT || user.getRole() == User.Role.FACULTY) 
                && !userService.hasAcceptedConsent(user.getId())) {
                return "redirect:/consent";
            }
            
            return "redirect:" + getRedirectUrlForRole(user.getRole());
        } catch (Exception e) {
            return "redirect:/login?error=true";
        }
    }
    
    private String getRedirectUrlForRole(User.Role role) {
        return switch (role) {
            case ADMIN -> "/admin/dashboard";
            case COUNSELLOR -> "/counsellor/dashboard";
            case FACULTY -> "/faculty/dashboard";
            case STUDENT -> "/student/dashboard";
        };
    }
}
