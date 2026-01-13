package com.dmhlh.service;

import com.dmhlh.entity.Nudge;
import com.dmhlh.entity.User;
import com.dmhlh.repository.NudgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NudgeService {
    
    private final NudgeRepository nudgeRepository;
    
    public NudgeService(NudgeRepository nudgeRepository) {
        this.nudgeRepository = nudgeRepository;
    }
    
    public List<Nudge> getActiveNudges(Long userId) {
        return nudgeRepository.findTop5ByUserIdAndDismissedAtIsNullOrderByCreatedAtDesc(userId);
    }
    
    public List<Nudge> getAllNudges(Long userId) {
        return nudgeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Transactional
    public Nudge createNudge(Long userId, String message, Nudge.TriggerType triggerType,
                            String actionUrl, String actionLabel) {
        User user = new User();
        user.setId(userId);
        
        Nudge nudge = Nudge.builder()
            .user(user)
            .message(message)
            .triggerType(triggerType)
            .actionUrl(actionUrl)
            .actionLabel(actionLabel)
            .build();
        
        return nudgeRepository.save(nudge);
    }
    
    @Transactional
    public void markAsSeen(Long nudgeId) {
        nudgeRepository.findById(nudgeId).ifPresent(nudge -> {
            if (nudge.getSeenAt() == null) {
                nudge.setSeenAt(LocalDateTime.now());
                nudgeRepository.save(nudge);
            }
        });
    }
    
    @Transactional
    public void dismiss(Long nudgeId) {
        nudgeRepository.findById(nudgeId).ifPresent(nudge -> {
            nudge.setDismissedAt(LocalDateTime.now());
            nudgeRepository.save(nudge);
        });
    }
    
    // Nudge message templates
    public static class NudgeTemplates {
        public static final String LOW_MOOD_STREAK = 
            "We've noticed your mood has been low lately. Remember, it's okay to seek support. Consider talking to a counsellor.";
        
        public static final String ASSESSMENT_DUE = 
            "It's been a while since your last self-assessment. Regular check-ins help track your wellbeing journey.";
        
        public static final String ASSESSMENT_MODERATE = 
            "Based on your recent assessment, you might benefit from speaking with a counsellor. They're here to help.";
        
        public static final String ASSESSMENT_SEVERE = 
            "Your recent assessment indicates you may be going through a difficult time. Please consider booking a counselling session soon.";
        
        public static final String LEARNING_SUGGESTION = 
            "Have you checked out our learning modules? They offer helpful strategies for managing stress and anxiety.";
    }
}
