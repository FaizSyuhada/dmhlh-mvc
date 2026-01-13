-- V6: Add more students (total 10) and comprehensive dummy data
-- Password: 'password' hashed with BCrypt

-- =====================================================
-- ADD 8 MORE STUDENTS (existing: student1, student2)
-- =====================================================
INSERT IGNORE INTO users (email, password_hash, display_name, role, enabled, created_at, updated_at) VALUES
('student3@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Charlie Davis', 'STUDENT', TRUE, NOW(), NOW()),
('student4@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Diana Martinez', 'STUDENT', TRUE, NOW(), NOW()),
('student5@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Ethan Williams', 'STUDENT', TRUE, NOW(), NOW()),
('student6@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Fiona Anderson', 'STUDENT', TRUE, NOW(), NOW()),
('student7@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'George Thompson', 'STUDENT', TRUE, NOW(), NOW()),
('student8@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Hannah Lee', 'STUDENT', TRUE, NOW(), NOW()),
('student9@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Ivan Garcia', 'STUDENT', TRUE, NOW(), NOW()),
('student10@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Julia Kim', 'STUDENT', TRUE, NOW(), NOW());

-- =====================================================
-- ADD CONSENTS FOR NEW STUDENTS  
-- =====================================================
INSERT IGNORE INTO consents (user_id, accepted, accepted_at, ip_address, created_at)
SELECT id, TRUE, NOW(), '127.0.0.1', NOW() FROM users WHERE email LIKE 'student%@dmhlh.test' AND id NOT IN (SELECT user_id FROM consents);

-- =====================================================
-- MOOD LOGS FOR STUDENTS (Last 7 days)
-- =====================================================

-- Student 1 (Alice Johnson) - Add more mood logs
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Had a productive day studying', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student1@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'Feeling a bit stressed about upcoming exam', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student1@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Good progress on my project', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM users WHERE email = 'student1@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 5, 'Got positive feedback from professor', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM users WHERE email = 'student1@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'Tired from late night studying', DATE_SUB(NOW(), INTERVAL 5 DAY) FROM users WHERE email = 'student1@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Relaxing weekend', DATE_SUB(NOW(), INTERVAL 6 DAY) FROM users WHERE email = 'student1@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'Monday blues', DATE_SUB(NOW(), INTERVAL 7 DAY) FROM users WHERE email = 'student1@dmhlh.test';

-- Student 3 (Charlie Davis) - Variable mood
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'Average day at university', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student3@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Met with study group, feeling motivated', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student3@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 2, 'Didnt sleep well last night', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM users WHERE email = 'student3@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Finished assignment early', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM users WHERE email = 'student3@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 5, 'Great workout session', DATE_SUB(NOW(), INTERVAL 5 DAY) FROM users WHERE email = 'student3@dmhlh.test';

-- Student 4 (Diana Martinez) - Generally positive
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 5, 'Love this course', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student4@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Coffee with friends', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student4@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Productive morning', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM users WHERE email = 'student4@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 5, 'Got accepted for internship', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM users WHERE email = 'student4@dmhlh.test';

-- Student 5 (Ethan Williams) - Needs support
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 2, 'Struggling with assignments', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student5@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 2, 'Feeling isolated', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student5@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'Talked to counsellor, feeling better', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM users WHERE email = 'student5@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 2, 'Cant focus on anything', DATE_SUB(NOW(), INTERVAL 4 DAY) FROM users WHERE email = 'student5@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 1, 'Really low day', DATE_SUB(NOW(), INTERVAL 5 DAY) FROM users WHERE email = 'student5@dmhlh.test';

-- Student 6-10 mood logs
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Good balance today', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student6@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Meditation helped', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student6@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'A bit tired', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM users WHERE email = 'student6@dmhlh.test';

INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Good day overall', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student7@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'Stressed about exams', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student7@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 5, 'Aced my presentation', DATE_SUB(NOW(), INTERVAL 3 DAY) FROM users WHERE email = 'student7@dmhlh.test';

INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 5, 'Feeling great', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student8@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Met new friends', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student8@dmhlh.test';

INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 3, 'Average day', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student9@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 2, 'Homesick', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student9@dmhlh.test';

INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 4, 'Making progress', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE email = 'student10@dmhlh.test';
INSERT INTO mood_logs (user_id, mood_value, note, created_at)
SELECT id, 5, 'Got scholarship news', DATE_SUB(NOW(), INTERVAL 2 DAY) FROM users WHERE email = 'student10@dmhlh.test';

-- =====================================================
-- USER POINTS FOR GAMIFICATION
-- =====================================================
INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 850, 5, 120, 450, 12, NOW(), NOW() FROM users WHERE email = 'student1@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 320, 3, 45, 180, 3, NOW(), NOW() FROM users WHERE email = 'student3@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 1250, 7, 200, 680, 21, NOW(), NOW() FROM users WHERE email = 'student4@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 150, 2, 30, 80, 2, NOW(), NOW() FROM users WHERE email = 'student5@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 720, 5, 90, 350, 8, NOW(), NOW() FROM users WHERE email = 'student6@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 480, 4, 75, 240, 5, NOW(), NOW() FROM users WHERE email = 'student7@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 980, 6, 150, 520, 15, NOW(), NOW() FROM users WHERE email = 'student8@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 280, 3, 40, 150, 4, NOW(), NOW() FROM users WHERE email = 'student9@dmhlh.test';

INSERT IGNORE INTO user_points (user_id, total_points, current_level, weekly_points, monthly_points, streak_days, created_at, updated_at)
SELECT id, 1100, 6, 180, 600, 18, NOW(), NOW() FROM users WHERE email = 'student10@dmhlh.test';

-- =====================================================
-- FORUM THREADS BY STUDENTS
-- =====================================================
INSERT INTO forum_threads (user_id, anonymous_alias, title, content, view_count, reply_count, created_at, updated_at)
SELECT id, 'HopefulHeart42', 'Tips for managing exam anxiety?', 
'Hi everyone! I have finals coming up and I am really struggling with anxiety. Does anyone have any tips that worked for them?', 
45, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM users WHERE email = 'student3@dmhlh.test';

INSERT INTO forum_threads (user_id, anonymous_alias, title, content, view_count, reply_count, created_at, updated_at)
SELECT id, 'CalmMind88', 'Meditation resources recommendations', 
'I recently started meditation and it has been really helpful. Wanted to share some apps I found useful: Headspace, Calm, and Insight Timer.', 
32, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM users WHERE email = 'student6@dmhlh.test';

INSERT INTO forum_threads (user_id, anonymous_alias, title, content, view_count, reply_count, created_at, updated_at)
SELECT id, 'SunnyDay55', 'Celebrating small wins!', 
'Just wanted to share that I completed my first week without missing any classes! It might seem small but its a big deal for me.', 
56, 2, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)
FROM users WHERE email = 'student8@dmhlh.test';
