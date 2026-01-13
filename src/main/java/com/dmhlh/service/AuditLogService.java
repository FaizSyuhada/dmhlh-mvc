package com.dmhlh.service;

import com.dmhlh.entity.AuditLog;
import com.dmhlh.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuditLogService {
    
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
    }
    
    @Async
    @Transactional
    public void logLogin(Long userId, String email, String ipAddress, String userAgent) {
        log(userId, email, AuditLog.ActionType.LOGIN, null, null, null, ipAddress, userAgent);
    }
    
    @Async
    @Transactional
    public void logLogout(Long userId, String email) {
        log(userId, email, AuditLog.ActionType.LOGOUT, null, null, null, null, null);
    }
    
    @Async
    @Transactional
    public void logConsentAccepted(Long userId, String email, String ipAddress) {
        log(userId, email, AuditLog.ActionType.CONSENT_ACCEPTED, "Consent", userId, null, ipAddress, null);
    }
    
    @Async
    @Transactional
    public void logConsentDeclined(Long userId, String email, String ipAddress) {
        log(userId, email, AuditLog.ActionType.CONSENT_DECLINED, "Consent", userId, null, ipAddress, null);
    }
    
    @Async
    @Transactional
    public void logAssessmentSubmitted(Long userId, String email, Long assessmentId, int score, String severity) {
        Map<String, Object> metadata = Map.of("score", score, "severity", severity);
        log(userId, email, AuditLog.ActionType.ASSESSMENT_SUBMITTED, "AssessmentResult", assessmentId, metadata, null, null);
    }
    
    @Async
    @Transactional
    public void logMoodLogged(Long userId, String email, Long moodLogId, int moodValue) {
        Map<String, Object> metadata = Map.of("moodValue", moodValue);
        log(userId, email, AuditLog.ActionType.MOOD_LOGGED, "MoodLog", moodLogId, metadata, null, null);
    }
    
    @Async
    @Transactional
    public void logAppointmentCreated(Long userId, String email, Long appointmentId) {
        log(userId, email, AuditLog.ActionType.APPOINTMENT_CREATED, "Appointment", appointmentId, null, null, null);
    }
    
    @Async
    @Transactional
    public void logAppointmentCancelled(Long userId, String email, Long appointmentId, String reason) {
        Map<String, Object> metadata = Map.of("reason", reason != null ? reason : "");
        log(userId, email, AuditLog.ActionType.APPOINTMENT_CANCELLED, "Appointment", appointmentId, metadata, null, null);
    }
    
    @Async
    @Transactional
    public void logPostCreated(Long userId, String email, Long postId, String type) {
        Map<String, Object> metadata = Map.of("type", type);
        log(userId, email, AuditLog.ActionType.POST_CREATED, "ForumPost", postId, metadata, null, null);
    }
    
    @Async
    @Transactional
    public void logPostReported(Long userId, String email, Long reportId, String reason) {
        Map<String, Object> metadata = Map.of("reason", reason);
        log(userId, email, AuditLog.ActionType.POST_REPORTED, "ForumReport", reportId, metadata, null, null);
    }
    
    @Async
    @Transactional
    public void logModerationAction(Long userId, String email, String entityType, Long entityId, String action) {
        Map<String, Object> metadata = Map.of("action", action);
        log(userId, email, AuditLog.ActionType.MODERATION_ACTION, entityType, entityId, metadata, null, null);
    }
    
    @Async
    @Transactional
    public void logReferralCreated(Long userId, String email, Long referralId) {
        log(userId, email, AuditLog.ActionType.REFERRAL_CREATED, "Referral", referralId, null, null, null);
    }
    
    @Async
    @Transactional
    public void logReferralStatusChanged(Long userId, String email, Long referralId, String newStatus) {
        Map<String, Object> metadata = Map.of("newStatus", newStatus);
        log(userId, email, AuditLog.ActionType.REFERRAL_STATUS_CHANGED, "Referral", referralId, metadata, null, null);
    }
    
    @Async
    @Transactional
    public void logProfileUpdated(Long userId, String email) {
        log(userId, email, AuditLog.ActionType.PROFILE_UPDATED, "User", userId, null, null, null);
    }
    
    @Async
    @Transactional
    public void logPasswordChanged(Long userId, String email) {
        log(userId, email, AuditLog.ActionType.PASSWORD_CHANGED, "User", userId, null, null, null);
    }
    
    @Async
    @Transactional
    public void logConsentUpdated(Long userId, String email) {
        log(userId, email, AuditLog.ActionType.CONSENT_UPDATED, "Consent", userId, null, null, null);
    }
    
    @Async
    @Transactional
    public void logConsentWithdrawn(Long userId, String email) {
        log(userId, email, AuditLog.ActionType.CONSENT_WITHDRAWN, "Consent", userId, null, null, null);
    }
    
    private void log(Long actorId, String actorEmail, AuditLog.ActionType actionType,
                     String entityType, Long entityId, Map<String, Object> metadata,
                     String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .actorId(actorId)
                .actorEmail(actorEmail)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .metadataJson(metadata != null ? objectMapper.writeValueAsString(metadata) : null)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
            
            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit metadata", e);
        }
    }
}
