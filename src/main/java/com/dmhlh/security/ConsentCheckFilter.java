package com.dmhlh.security;

import com.dmhlh.entity.User;
import com.dmhlh.repository.ConsentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class ConsentCheckFilter extends OncePerRequestFilter {
    
    private final ConsentRepository consentRepository;
    
    // Paths that don't require consent
    private static final Set<String> EXEMPT_PATHS = Set.of(
        "/consent",
        "/profile",
        "/login",
        "/logout",
        "/css",
        "/js",
        "/images",
        "/error",
        "/403",
        "/404",
        "/favicon.ico"
    );
    
    // Public paths accessible without consent (learning modules for declined consent)
    private static final Set<String> PUBLIC_LEARNING_PATHS = Set.of(
        "/student/modules"
    );
    
    public ConsentCheckFilter(ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip filter for exempt paths
        if (isExemptPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Skip if not authenticated
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        
        // Admin and Counsellors don't need consent check for their work
        if (userDetails.getRole() == User.Role.ADMIN || userDetails.getRole() == User.Role.COUNSELLOR) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check consent for students and faculty
        boolean hasConsent = consentRepository.existsByUserIdAndAcceptedTrue(userDetails.getId());
        
        if (!hasConsent) {
            // Allow public learning paths even without consent
            if (isPublicLearningPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Redirect to consent page
            response.sendRedirect(request.getContextPath() + "/consent");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isExemptPath(String path) {
        for (String exempt : EXEMPT_PATHS) {
            if (path.equals(exempt) || path.startsWith(exempt + "/") || path.startsWith(exempt + "?")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isPublicLearningPath(String path) {
        for (String publicPath : PUBLIC_LEARNING_PATHS) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/") || path.startsWith(publicPath + "?")) {
                return true;
            }
        }
        return false;
    }
}
