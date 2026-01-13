package com.dmhlh.service;

import com.dmhlh.entity.*;
import com.dmhlh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserPointsRepository userPointsRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final MoodLogRepository moodLogRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final AppointmentRepository appointmentRepository;

    // Point values for different actions
    private static final int POINTS_MOOD_LOG = 10;
    private static final int POINTS_ASSESSMENT = 25;
    private static final int POINTS_MODULE_COMPLETE = 50;
    private static final int POINTS_QUIZ_PASS = 30;
    private static final int POINTS_FORUM_POST = 15;
    private static final int POINTS_FORUM_HELPFUL = 5;
    private static final int POINTS_APPOINTMENT = 20;
    private static final int POINTS_STREAK_BONUS = 50;

    @Transactional
    public UserPoints getOrCreateUserPoints(User user) {
        return userPointsRepository.findByUser(user)
                .orElseGet(() -> {
                    UserPoints newPoints = UserPoints.builder()
                            .user(user)
                            .totalPoints(0)
                            .currentLevel(1)
                            .xpToNextLevel(1000)
                            .currentStreak(0)
                            .longestStreak(0)
                            .build();
                    return userPointsRepository.save(newPoints);
                });
    }

    @Transactional
    public void awardPoints(User user, int points, PointTransaction.TransactionType type, 
                          PointTransaction.Source source, Long sourceId, String description) {
        UserPoints userPoints = getOrCreateUserPoints(user);
        userPoints.addPoints(points);
        userPoints.setLastActivityAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);

        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .points(points)
                .transactionType(type)
                .source(source)
                .sourceId(sourceId)
                .description(description)
                .build();
        pointTransactionRepository.save(transaction);

        // Check for badge eligibility after awarding points
        checkAndAwardBadges(user);
        
        log.info("Awarded {} points to user {} for {}", points, user.getEmail(), description);
    }

    @Transactional
    public void awardPointsForMoodLog(User user, Long moodLogId) {
        awardPoints(user, POINTS_MOOD_LOG, PointTransaction.TransactionType.EARNED,
                PointTransaction.Source.MOOD_LOG, moodLogId, "Logged daily mood");
        updateStreak(user);
    }

    @Transactional
    public void awardPointsForAssessment(User user, Long assessmentId) {
        awardPoints(user, POINTS_ASSESSMENT, PointTransaction.TransactionType.EARNED,
                PointTransaction.Source.ASSESSMENT, assessmentId, "Completed assessment");
    }

    @Transactional
    public void awardPointsForModuleComplete(User user, Long moduleId) {
        awardPoints(user, POINTS_MODULE_COMPLETE, PointTransaction.TransactionType.EARNED,
                PointTransaction.Source.MODULE_COMPLETE, moduleId, "Completed learning module");
    }

    @Transactional
    public void awardPointsForQuizPass(User user, Long quizAttemptId) {
        awardPoints(user, POINTS_QUIZ_PASS, PointTransaction.TransactionType.EARNED,
                PointTransaction.Source.QUIZ_PASS, quizAttemptId, "Passed module quiz");
    }

    @Transactional
    public void awardPointsForForumPost(User user, Long threadId) {
        awardPoints(user, POINTS_FORUM_POST, PointTransaction.TransactionType.EARNED,
                PointTransaction.Source.FORUM_POST, threadId, "Created forum post");
    }

    @Transactional
    public void awardPointsForAppointment(User user, Long appointmentId) {
        awardPoints(user, POINTS_APPOINTMENT, PointTransaction.TransactionType.EARNED,
                PointTransaction.Source.APPOINTMENT, appointmentId, "Attended counselling session");
    }

    @Transactional
    public void updateStreak(User user) {
        UserPoints userPoints = getOrCreateUserPoints(user);
        LocalDateTime lastActivity = userPoints.getLastActivityAt();
        LocalDateTime now = LocalDateTime.now();
        
        if (lastActivity == null) {
            // First activity ever - start streak at 1
            userPoints.setCurrentStreak(1);
        } else if (lastActivity.toLocalDate().isBefore(now.toLocalDate().minusDays(1))) {
            // Streak broken (more than 1 day gap) - reset to 1
            userPoints.setCurrentStreak(1);
        } else if (lastActivity.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            // Consecutive day - increase streak
            userPoints.setCurrentStreak(userPoints.getCurrentStreak() + 1);
            
            // Award streak bonus at milestones
            if (userPoints.getCurrentStreak() % 7 == 0) {
                awardPoints(user, POINTS_STREAK_BONUS, PointTransaction.TransactionType.BONUS,
                        PointTransaction.Source.STREAK_BONUS, null, 
                        userPoints.getCurrentStreak() + " day streak bonus!");
            }
        } else if (lastActivity.toLocalDate().equals(now.toLocalDate())) {
            // Same day - ensure streak is at least 1
            if (userPoints.getCurrentStreak() == 0) {
                userPoints.setCurrentStreak(1);
            }
            // Don't update lastActivityAt again if same day, just return
            userPointsRepository.save(userPoints);
            return;
        }
        
        // Update longest streak
        if (userPoints.getCurrentStreak() > userPoints.getLongestStreak()) {
            userPoints.setLongestStreak(userPoints.getCurrentStreak());
        }
        
        userPoints.setLastActivityAt(now);
        userPointsRepository.save(userPoints);
    }

    @Transactional
    public void checkAndAwardBadges(User user) {
        List<Badge> allBadges = badgeRepository.findByActiveTrue();
        
        for (Badge badge : allBadges) {
            if (userBadgeRepository.existsByUserAndBadge(user, badge)) {
                continue; // Already has this badge
            }
            
            boolean earned = checkBadgeRequirement(user, badge);
            if (earned) {
                awardBadge(user, badge);
            }
        }
    }

    private boolean checkBadgeRequirement(User user, Badge badge) {
        int required = badge.getRequirementValue();
        
        switch (badge.getRequirementType()) {
            case MODULES_COMPLETED:
                return quizAttemptRepository.countDistinctModulesByUserAndPassed(user.getId()) >= required;
            case ASSESSMENTS_TAKEN:
                return assessmentResultRepository.countByUserId(user.getId()) >= required;
            case MOOD_STREAK:
                UserPoints points = getOrCreateUserPoints(user);
                return points.getLongestStreak() >= required || points.getCurrentStreak() >= required;
            case FORUM_POSTS:
                return forumThreadRepository.countByAuthorId(user.getId()) >= required;
            case APPOINTMENTS_ATTENDED:
                return appointmentRepository.countByStudentIdAndStatus(user.getId(), Appointment.Status.COMPLETED) >= required;
            case POINTS_EARNED:
                UserPoints userPoints = getOrCreateUserPoints(user);
                return userPoints.getTotalPoints() >= required;
            case CUSTOM:
                // Custom badges are awarded manually
                return false;
            default:
                return false;
        }
    }

    @Transactional
    public void awardBadge(User user, Badge badge) {
        if (userBadgeRepository.existsByUserAndBadge(user, badge)) {
            return;
        }

        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .build();
        userBadgeRepository.save(userBadge);

        // Award bonus points for earning the badge
        if (badge.getPointsValue() > 0) {
            awardPoints(user, badge.getPointsValue(), PointTransaction.TransactionType.BADGE_REWARD,
                    PointTransaction.Source.BADGE, badge.getId(), 
                    "Earned badge: " + badge.getName());
        }
        
        log.info("User {} earned badge: {}", user.getEmail(), badge.getName());
    }

    public List<UserBadge> getUserBadges(User user) {
        return userBadgeRepository.findByUserIdWithBadge(user.getId());
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findByActiveTrueOrderByPointsValueDesc();
    }

    public List<UserPoints> getLeaderboard() {
        return userPointsRepository.findAllByOrderByTotalPointsDesc();
    }

    public int getUserRank(User user) {
        return userPointsRepository.findRankByUserId(user.getId());
    }

    public List<PointTransaction> getRecentTransactions(User user, int limit) {
        return pointTransactionRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
                user, LocalDateTime.now().minusDays(30));
    }
}
