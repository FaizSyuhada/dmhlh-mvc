-- V4: Add Gamification, Improved Appointments, and Forum enhancements
-- =====================================================================

-- ===================
-- GAMIFICATION SYSTEM
-- ===================

-- Badges definitions table
CREATE TABLE badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon_url VARCHAR(500),
    category ENUM('LEARNING', 'ENGAGEMENT', 'WELLNESS', 'COMMUNITY', 'ACHIEVEMENT') NOT NULL,
    points_value INT NOT NULL DEFAULT 0,
    requirement_type ENUM('MODULES_COMPLETED', 'ASSESSMENTS_TAKEN', 'MOOD_STREAK', 'FORUM_POSTS', 'APPOINTMENTS_ATTENDED', 'POINTS_EARNED', 'CUSTOM') NOT NULL,
    requirement_value INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    INDEX idx_badges_category (category),
    INDEX idx_badges_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User badges (earned badges)
CREATE TABLE user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_badge (user_id, badge_id),
    INDEX idx_user_badges_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User points table
CREATE TABLE user_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_points INT NOT NULL DEFAULT 0,
    current_level INT NOT NULL DEFAULT 1,
    xp_to_next_level INT NOT NULL DEFAULT 1000,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_activity_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Point transactions (history)
CREATE TABLE point_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INT NOT NULL,
    transaction_type ENUM('EARNED', 'SPENT', 'BONUS', 'BADGE_REWARD') NOT NULL,
    source ENUM('MOOD_LOG', 'ASSESSMENT', 'MODULE_COMPLETE', 'QUIZ_PASS', 'FORUM_POST', 'FORUM_HELPFUL', 'APPOINTMENT', 'STREAK_BONUS', 'BADGE', 'ADMIN') NOT NULL,
    source_id BIGINT,
    description VARCHAR(255),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_point_transactions_user (user_id),
    INDEX idx_point_transactions_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ======================
-- IMPROVED APPOINTMENTS
-- ======================

-- Counsellor profiles (extended info)
CREATE TABLE counsellor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    title VARCHAR(50),
    specializations TEXT,
    bio TEXT,
    years_experience INT,
    profile_image_url VARCHAR(500),
    rating_average DECIMAL(3,2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,
    max_daily_appointments INT DEFAULT 8,
    appointment_duration_minutes INT DEFAULT 50,
    buffer_minutes INT DEFAULT 10,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Counsellor availability slots
CREATE TABLE counsellor_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counsellor_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT TRUE,
    specific_date DATE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (counsellor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_availability_counsellor (counsellor_id),
    INDEX idx_availability_day (day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Counsellor blocked dates (holidays, leave)
CREATE TABLE counsellor_blocked_dates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counsellor_id BIGINT NOT NULL,
    blocked_date DATE NOT NULL,
    reason VARCHAR(255),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (counsellor_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_counsellor_blocked_date (counsellor_id, blocked_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Session types
CREATE TABLE session_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    duration_minutes INT NOT NULL DEFAULT 50,
    icon VARCHAR(50),
    is_free BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add session_type to appointments
ALTER TABLE appointments 
ADD COLUMN session_type_id BIGINT AFTER counsellor_id,
ADD COLUMN meeting_link VARCHAR(500) AFTER cancellation_reason,
ADD COLUMN student_notes TEXT AFTER meeting_link,
ADD COLUMN rating INT AFTER student_notes,
ADD COLUMN rating_comment TEXT AFTER rating,
ADD FOREIGN KEY (session_type_id) REFERENCES session_types(id) ON DELETE SET NULL;

-- ======================
-- IMPROVED FORUM
-- ======================

-- Thread tags
CREATE TABLE forum_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(7) DEFAULT '#6B7280',
    description VARCHAR(255),
    post_count INT DEFAULT 0,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thread-tag relationship
CREATE TABLE forum_thread_tags (
    thread_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (thread_id, tag_id),
    FOREIGN KEY (thread_id) REFERENCES forum_threads(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES forum_tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thread likes/upvotes
CREATE TABLE forum_thread_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (thread_id) REFERENCES forum_threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_thread_like (thread_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Post likes/upvotes
CREATE TABLE forum_post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_like (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thread bookmarks
CREATE TABLE forum_bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (thread_id) REFERENCES forum_threads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_bookmark (thread_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thread drafts
CREATE TABLE forum_drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    content TEXT,
    tags_json TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_drafts_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add like_count and reply_count to threads for performance
ALTER TABLE forum_threads
ADD COLUMN like_count INT DEFAULT 0 AFTER view_count,
ADD COLUMN reply_count INT DEFAULT 0 AFTER like_count;

-- Add like_count to posts
ALTER TABLE forum_posts
ADD COLUMN like_count INT DEFAULT 0 AFTER status;

-- ===================
-- SEED DATA
-- ===================

-- Insert badges
INSERT INTO badges (code, name, description, icon_url, category, points_value, requirement_type, requirement_value, active, created_at) VALUES
('first_step', 'First Step', 'Complete your first learning module', '🎯', 'LEARNING', 50, 'MODULES_COMPLETED', 1, TRUE, NOW()),
('knowledge_seeker', 'Knowledge Seeker', 'Complete 3 learning modules', '📚', 'LEARNING', 100, 'MODULES_COMPLETED', 3, TRUE, NOW()),
('scholar', 'Scholar', 'Complete all learning modules', '🎓', 'LEARNING', 250, 'MODULES_COMPLETED', 5, TRUE, NOW()),
('self_aware', 'Self Aware', 'Take your first assessment', '🪞', 'WELLNESS', 50, 'ASSESSMENTS_TAKEN', 1, TRUE, NOW()),
('wellness_warrior', 'Wellness Warrior', 'Take 5 assessments', '💪', 'WELLNESS', 150, 'ASSESSMENTS_TAKEN', 5, TRUE, NOW()),
('mood_tracker', 'Mood Tracker', 'Log your mood for 7 consecutive days', '📊', 'WELLNESS', 100, 'MOOD_STREAK', 7, TRUE, NOW()),
('consistency_king', 'Consistency King', 'Maintain a 30-day streak', '👑', 'WELLNESS', 500, 'MOOD_STREAK', 30, TRUE, NOW()),
('community_member', 'Community Member', 'Create your first forum post', '💬', 'COMMUNITY', 50, 'FORUM_POSTS', 1, TRUE, NOW()),
('helpful_hand', 'Helpful Hand', 'Get 10 likes on your posts', '🤝', 'COMMUNITY', 100, 'CUSTOM', 10, TRUE, NOW()),
('rising_star', 'Rising Star', 'Earn 500 points', '⭐', 'ACHIEVEMENT', 0, 'POINTS_EARNED', 500, TRUE, NOW()),
('superstar', 'Superstar', 'Earn 2000 points', '🌟', 'ACHIEVEMENT', 0, 'POINTS_EARNED', 2000, TRUE, NOW()),
('session_starter', 'Session Starter', 'Attend your first counselling session', '🗓️', 'ENGAGEMENT', 75, 'APPOINTMENTS_ATTENDED', 1, TRUE, NOW());

-- Insert session types
INSERT INTO session_types (name, description, duration_minutes, icon, is_free, active, display_order, created_at) VALUES
('Video Call', 'Face-to-face video consultation with screen sharing capability', 50, '📹', TRUE, TRUE, 1, NOW()),
('Chat Session', 'Text-based counselling session for those who prefer typing', 50, '💬', TRUE, TRUE, 2, NOW()),
('Phone Call', 'Voice call for audio-only consultation', 30, '📞', TRUE, TRUE, 3, NOW()),
('In-Person', 'Meet your counsellor at the wellness center', 50, '🏢', TRUE, TRUE, 4, NOW());

-- Insert forum tags
INSERT INTO forum_tags (name, color, description, created_at) VALUES
('anxiety', '#EF4444', 'Discussions about anxiety and coping strategies', NOW()),
('depression', '#3B82F6', 'Support for those dealing with depression', NOW()),
('stress', '#F59E0B', 'Managing stress and burnout', NOW()),
('relationships', '#EC4899', 'Relationship advice and support', NOW()),
('academic', '#8B5CF6', 'Academic pressure and study tips', NOW()),
('self-care', '#10B981', 'Self-care routines and wellness tips', NOW()),
('success-story', '#22C55E', 'Share your wins and progress', NOW()),
('question', '#6366F1', 'Ask the community for advice', NOW()),
('resources', '#14B8A6', 'Helpful resources and recommendations', NOW()),
('general', '#6B7280', 'General discussion', NOW());

-- Insert sample counsellor profiles
INSERT INTO counsellor_profiles (user_id, title, specializations, bio, years_experience, rating_average, rating_count, active, created_at)
SELECT id, 'Dr.', 'Anxiety, Depression, Academic Stress', 'Experienced counsellor specializing in student mental health with a focus on evidence-based approaches.', 8, 4.8, 24, TRUE, NOW()
FROM users WHERE role = 'COUNSELLOR' LIMIT 1;

-- Insert sample availability for counsellors (Mon-Fri, 9AM-5PM)
INSERT INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, d.day, '09:00:00', '17:00:00', TRUE, TRUE, NOW()
FROM users u
CROSS JOIN (SELECT 1 as day UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) d
WHERE u.role = 'COUNSELLOR';

-- Initialize user_points for existing students
INSERT INTO user_points (user_id, total_points, current_level, xp_to_next_level, current_streak, created_at)
SELECT id, 0, 1, 1000, 0, NOW()
FROM users WHERE role = 'STUDENT'
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Sample forum threads with likes and tags
INSERT INTO forum_threads (author_id, anonymous_alias, title, content, status, view_count, like_count, reply_count, created_at) VALUES
((SELECT id FROM users WHERE role = 'STUDENT' LIMIT 1), 'HopefulPanda', 'How do you deal with exam anxiety?', 'Finals are coming up and I''m feeling really overwhelmed. Does anyone have tips for managing exam stress? I''ve tried deep breathing but it doesn''t seem to help much. Would love to hear what works for others! 💭', 'ACTIVE', 156, 23, 8, NOW() - INTERVAL 2 DAY),
((SELECT id FROM users WHERE role = 'STUDENT' LIMIT 1 OFFSET 1), 'CalmButterfly', 'Small wins thread! Share your victories 🎉', 'I''ll start - I actually got out of bed before noon today and made myself breakfast. It might seem small but it''s huge for me right now. What''s your win today?', 'ACTIVE', 89, 45, 12, NOW() - INTERVAL 1 DAY),
((SELECT id FROM users WHERE role = 'STUDENT' LIMIT 1), 'QuietMountain', 'Feeling alone in a crowded campus', 'Is it just me or does anyone else feel invisible sometimes? I walk through crowds of people every day but feel completely alone. How do you make genuine connections here?', 'ACTIVE', 234, 67, 15, NOW() - INTERVAL 3 HOUR);

-- Link tags to threads
INSERT INTO forum_thread_tags (thread_id, tag_id)
SELECT ft.id, t.id FROM forum_threads ft, forum_tags t 
WHERE ft.title LIKE '%exam%' AND t.name IN ('anxiety', 'academic', 'stress') LIMIT 3;

INSERT INTO forum_thread_tags (thread_id, tag_id)
SELECT ft.id, t.id FROM forum_threads ft, forum_tags t 
WHERE ft.title LIKE '%wins%' AND t.name IN ('success-story', 'self-care') LIMIT 2;

INSERT INTO forum_thread_tags (thread_id, tag_id)
SELECT ft.id, t.id FROM forum_threads ft, forum_tags t 
WHERE ft.title LIKE '%alone%' AND t.name IN ('relationships', 'general') LIMIT 2;
