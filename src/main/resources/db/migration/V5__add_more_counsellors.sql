-- V5: Add more counsellors with varied availability and session types

-- Add more counsellor users
INSERT IGNORE INTO users (email, password_hash, display_name, role, enabled, created_at) VALUES
('counsellor2@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Michael Roberts', 'COUNSELLOR', TRUE, NOW()),
('counsellor3@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Emily Watson', 'COUNSELLOR', TRUE, NOW()),
('counsellor4@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Dr. James Miller', 'COUNSELLOR', TRUE, NOW()),
('counsellor5@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Lisa Thompson', 'COUNSELLOR', TRUE, NOW());

-- Create counsellor profiles with different specializations
INSERT IGNORE INTO counsellor_profiles (user_id, title, specializations, bio, years_experience, rating_average, rating_count, active, created_at)
SELECT id, '', 'Relationships, Social Anxiety, Self-Esteem', 
'Specializing in interpersonal relationships and social confidence building. I believe in creating a safe, non-judgmental space for students to explore their feelings.', 
5, 4.9, 31, TRUE, NOW()
FROM users WHERE email = 'counsellor2@dmhlh.test';

INSERT IGNORE INTO counsellor_profiles (user_id, title, specializations, bio, years_experience, rating_average, rating_count, active, created_at)
SELECT id, 'Ms.', 'Stress Management, Mindfulness, Work-Life Balance', 
'Certified mindfulness practitioner with expertise in stress reduction techniques. I help students find balance between academics and personal well-being.', 
6, 4.7, 45, TRUE, NOW()
FROM users WHERE email = 'counsellor3@dmhlh.test';

INSERT IGNORE INTO counsellor_profiles (user_id, title, specializations, bio, years_experience, rating_average, rating_count, active, created_at)
SELECT id, 'Dr.', 'Trauma, PTSD, Grief Counseling', 
'Clinical psychologist with 12 years of experience in trauma-informed care. I specialize in helping students process difficult life experiences.', 
12, 4.95, 67, TRUE, NOW()
FROM users WHERE email = 'counsellor4@dmhlh.test';

INSERT IGNORE INTO counsellor_profiles (user_id, title, specializations, bio, years_experience, rating_average, rating_count, active, created_at)
SELECT id, '', 'Career Anxiety, Decision Making, Life Transitions', 
'Helping students navigate major life decisions and career-related stress. Together we can build confidence in your future path.', 
4, 4.6, 19, TRUE, NOW()
FROM users WHERE email = 'counsellor5@dmhlh.test';

-- Add varied availability for new counsellors

-- Michael Roberts: Evening specialist (Mon, Wed, Fri evenings)
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 1, '17:00:00', '21:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor2@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 3, '17:00:00', '21:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor2@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 5, '17:00:00', '21:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor2@dmhlh.test';

-- Emily Watson: Morning specialist (Tue, Thu, Sat mornings)
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 2, '07:00:00', '12:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor3@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 4, '07:00:00', '12:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor3@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 6, '09:00:00', '14:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor3@dmhlh.test';

-- Dr. James Miller: Full weekdays (limited slots - senior counsellor)
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 1, '10:00:00', '15:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor4@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 2, '10:00:00', '15:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor4@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 3, '10:00:00', '15:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor4@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 4, '10:00:00', '15:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor4@dmhlh.test';

-- Lisa Thompson: Flexible schedule (Mon-Fri afternoons, weekends)
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 1, '13:00:00', '18:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor5@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 2, '13:00:00', '18:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor5@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 3, '13:00:00', '18:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor5@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 4, '13:00:00', '18:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor5@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 5, '13:00:00', '18:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor5@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 6, '10:00:00', '16:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor5@dmhlh.test';
INSERT IGNORE INTO counsellor_availability (counsellor_id, day_of_week, start_time, end_time, is_recurring, is_available, created_at)
SELECT u.id, 0, '10:00:00', '14:00:00', TRUE, TRUE, NOW() FROM users u WHERE u.email = 'counsellor5@dmhlh.test';

-- Add more session types
INSERT IGNORE INTO session_types (name, description, duration_minutes, icon, is_free, active, display_order, created_at) VALUES
('Group Session', 'Join a small group session with peers facing similar challenges', 90, '👥', TRUE, TRUE, 4, NOW()),
('Crisis Support', 'Immediate support for urgent mental health concerns', 30, '🆘', TRUE, TRUE, 5, NOW()),
('Follow-up Session', 'Brief check-in session for ongoing support', 25, '🔄', TRUE, TRUE, 6, NOW()),
('Wellness Workshop', 'Interactive workshop on mental wellness topics', 60, '🧘', TRUE, TRUE, 7, NOW());
