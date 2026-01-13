package com.dmhlh.security;

import com.dmhlh.entity.User;
import com.dmhlh.repository.ConsentRepository;
import com.dmhlh.service.AuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final ConsentRepository consentRepository;
    private final AuditLogService auditLogService;
    
    public CustomAuthenticationSuccessHandler(ConsentRepository consentRepository,
                                              AuditLogService auditLogService) {
        this.consentRepository = consentRepository;
        this.auditLogService = auditLogService;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // Log the login
        auditLogService.logLogin(userDetails.getId(), userDetails.getEmail(), 
                                request.getRemoteAddr(), request.getHeader("User-Agent"));
        
        // Check consent for students and faculty
        if (userDetails.getRole() == User.Role.STUDENT || userDetails.getRole() == User.Role.FACULTY) {
            boolean hasConsent = consentRepository.existsByUserIdAndAcceptedTrue(userDetails.getId());
            if (!hasConsent) {
                response.sendRedirect(request.getContextPath() + "/consent");
                return;
            }
        }
        
        // Redirect based on role
        String targetUrl = getTargetUrlForRole(userDetails.getRole());
        response.sendRedirect(request.getContextPath() + targetUrl);
    }
    
    private String getTargetUrlForRole(User.Role role) {
        return switch (role) {
            case ADMIN -> "/admin/dashboard";
            case COUNSELLOR -> "/counsellor/dashboard";
            case FACULTY -> "/faculty/dashboard";
            case STUDENT -> "/student/dashboard";
        };
    }
}
