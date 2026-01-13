package com.dmhlh.service;

import com.dmhlh.entity.MoodLog;
import com.dmhlh.entity.User;
import com.dmhlh.repository.MoodLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MoodLogService {
    
    private final MoodLogRepository moodLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public MoodLogService(MoodLogRepository moodLogRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.moodLogRepository = moodLogRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public List<MoodLog> getRecentLogs(Long userId, int count) {
        if (count == 7) {
            return moodLogRepository.findTop7ByUserIdOrderByCreatedAtDesc(userId);
        }
        return moodLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<MoodLog> getLogsSince(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return moodLogRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);
    }
    
    public Optional<Double> getAverageMood(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Double avg = moodLogRepository.getAverageMoodSince(userId, since);
        return Optional.ofNullable(avg);
    }
    
    @Transactional
    public MoodLog logMood(Long userId, int moodValue, String note) {
        User user = new User();
        user.setId(userId);
        
        MoodLog moodLog = MoodLog.builder()
            .user(user)
            .moodValue(moodValue)
            .note(note)
            .build();
        
        MoodLog saved = moodLogRepository.save(moodLog);
        
        // Publish event for Observer pattern
        eventPublisher.publishEvent(new MoodLoggedEvent(this, saved));
        
        return saved;
    }
    
    public boolean hasLowMoodStreak(Long userId, int streakSize, int threshold) {
        List<MoodLog> recentLogs = moodLogRepository.findTop7ByUserIdOrderByCreatedAtDesc(userId);
        
        if (recentLogs.size() < streakSize) {
            return false;
        }
        
        // Check if last 'streakSize' moods are <= threshold
        return recentLogs.stream()
            .limit(streakSize)
            .allMatch(log -> log.getMoodValue() <= threshold);
    }
    
    public long countTotal() {
        return moodLogRepository.countTotal();
    }
    
    public long countSince(LocalDateTime since) {
        return moodLogRepository.countSince(since);
    }
    
    // Event class for Observer pattern
    public static class MoodLoggedEvent {
        private final Object source;
        private final MoodLog moodLog;
        
        public MoodLoggedEvent(Object source, MoodLog moodLog) {
            this.source = source;
            this.moodLog = moodLog;
        }
        
        public Object getSource() {
            return source;
        }
        
        public MoodLog getMoodLog() {
            return moodLog;
        }
    }
}
