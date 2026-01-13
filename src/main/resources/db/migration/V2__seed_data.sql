-- DMHLH Seed Data
-- V2: Demo users, content, and sample data

-- =====================================================
-- USERS (password: 'password' hashed with BCrypt)
-- =====================================================
INSERT INTO users (email, password_hash, display_name, role, enabled, created_at, updated_at) VALUES
('admin@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'System Admin', 'ADMIN', TRUE, NOW(), NOW()),
('counsellor@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Dr. Sarah Chen', 'COUNSELLOR', TRUE, NOW(), NOW()),
('counsellor2@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Dr. Michael Brown', 'COUNSELLOR', TRUE, NOW(), NOW()),
('faculty@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Prof. James Wilson', 'FACULTY', TRUE, NOW(), NOW()),
('student1@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alice Johnson', 'STUDENT', TRUE, NOW(), NOW()),
('student2@dmhlh.test', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Bob Smith', 'STUDENT', TRUE, NOW(), NOW());

-- =====================================================
-- CONSENTS (student1 accepted, student2 pending)
-- =====================================================
INSERT INTO consents (user_id, accepted, accepted_at, ip_address, created_at) VALUES
(1, TRUE, NOW(), '127.0.0.1', NOW()),  -- admin
(2, TRUE, NOW(), '127.0.0.1', NOW()),  -- counsellor
(3, TRUE, NOW(), '127.0.0.1', NOW()),  -- counsellor2
(4, TRUE, NOW(), '127.0.0.1', NOW()),  -- faculty
(5, TRUE, NOW(), '127.0.0.1', NOW());  -- student1

-- =====================================================
-- LEARNING MODULES
-- =====================================================
INSERT INTO learning_modules (title, description, type, body_content, video_url, status, display_order, created_by, created_at, updated_at) VALUES
('Understanding Anxiety', 
 'Learn about the different types of anxiety and how to recognize the symptoms.',
 'MIXED',
 '<h2>What is Anxiety?</h2>
<p>Anxiety is your body''s natural response to stress. It''s a feeling of fear or apprehension about what''s to come.</p>

<h3>Common Types of Anxiety Disorders</h3>
<ul>
    <li><strong>Generalized Anxiety Disorder (GAD)</strong> - Excessive worry about everyday things</li>
    <li><strong>Social Anxiety Disorder</strong> - Fear of social situations</li>
    <li><strong>Panic Disorder</strong> - Sudden episodes of intense fear</li>
    <li><strong>Specific Phobias</strong> - Intense fear of specific objects or situations</li>
</ul>

<h3>Physical Symptoms</h3>
<p>Anxiety can manifest physically as:</p>
<ul>
    <li>Rapid heartbeat</li>
    <li>Sweating</li>
    <li>Trembling</li>
    <li>Shortness of breath</li>
    <li>Muscle tension</li>
</ul>

<h3>Coping Strategies</h3>
<p>There are many effective ways to manage anxiety:</p>
<ol>
    <li>Practice deep breathing exercises</li>
    <li>Regular physical exercise</li>
    <li>Maintain a consistent sleep schedule</li>
    <li>Limit caffeine and alcohol</li>
    <li>Talk to someone you trust</li>
</ol>',
 'https://www.youtube.com/embed/BVJkf4FEPcc',
 'PUBLISHED', 1, 1, NOW(), NOW()),

('Managing Academic Stress',
 'Practical strategies for handling the pressures of student life.',
 'TEXT',
 '<h2>Academic Stress: A Student''s Guide</h2>

<p>Feeling stressed about exams, deadlines, or academic performance is completely normal. What matters is how you manage that stress.</p>

<h3>Why Do Students Experience Stress?</h3>
<ul>
    <li>Heavy course loads</li>
    <li>Exam pressure</li>
    <li>Time management challenges</li>
    <li>Financial concerns</li>
    <li>Social pressures</li>
    <li>Future career uncertainty</li>
</ul>

<h3>Time Management Tips</h3>
<ol>
    <li><strong>Use a planner</strong> - Write down all assignments and deadlines</li>
    <li><strong>Break tasks into smaller chunks</strong> - Large projects feel less overwhelming</li>
    <li><strong>Prioritize</strong> - Focus on what''s most important first</li>
    <li><strong>Avoid procrastination</strong> - Start early, even if just for 15 minutes</li>
    <li><strong>Schedule breaks</strong> - Your brain needs rest to function well</li>
</ol>

<h3>Self-Care Essentials</h3>
<p>Don''t neglect your basic needs:</p>
<ul>
    <li>Get 7-9 hours of sleep</li>
    <li>Eat nutritious meals</li>
    <li>Exercise regularly</li>
    <li>Stay connected with friends and family</li>
    <li>Make time for activities you enjoy</li>
</ul>

<h3>When to Seek Help</h3>
<p>If stress is significantly impacting your daily life, academic performance, or relationships, consider reaching out to a counselor. There''s no shame in asking for help.</p>',
 NULL,
 'PUBLISHED', 2, 1, NOW(), NOW()),

('Building Healthy Relationships',
 'Understanding boundaries, communication, and emotional intelligence.',
 'TEXT',
 '<h2>Draft Content - Coming Soon</h2>
<p>This module is currently being developed.</p>',
 NULL,
 'DRAFT', 3, 1, NOW(), NOW());

-- =====================================================
-- QUIZ QUESTIONS (for Module 1: Understanding Anxiety)
-- =====================================================
INSERT INTO quiz_questions (module_id, question, option_a, option_b, option_c, option_d, correct_option, explanation, display_order, created_at) VALUES
(1, 'Which of the following is NOT a common type of anxiety disorder?', 
 'Generalized Anxiety Disorder', 'Social Anxiety Disorder', 'Academic Anxiety Disorder', 'Panic Disorder',
 'C', 'Academic Anxiety Disorder is not a recognized clinical diagnosis. The common types include GAD, Social Anxiety, Panic Disorder, and Specific Phobias.', 1, NOW()),

(1, 'What is a common physical symptom of anxiety?',
 'Increased appetite', 'Rapid heartbeat', 'Improved sleep', 'Decreased muscle tension',
 'B', 'Anxiety often manifests as rapid heartbeat, along with sweating, trembling, and muscle tension.', 2, NOW()),

(1, 'Which coping strategy is recommended for managing anxiety?',
 'Increasing caffeine intake', 'Avoiding all social situations', 'Practicing deep breathing exercises', 'Staying up late to study more',
 'C', 'Deep breathing exercises are an effective way to manage anxiety. Caffeine should be limited, and sleep is important.', 3, NOW()),

(1, 'What should you do if anxiety significantly impacts your daily life?',
 'Ignore it and hope it goes away', 'Seek help from a counselor', 'Isolate yourself from others', 'Take unverified supplements',
 'B', 'If anxiety significantly impacts your life, it''s important to seek professional help from a counselor or mental health professional.', 4, NOW());

-- =====================================================
-- ASSESSMENT DEFINITIONS (PHQ-9)
-- =====================================================
INSERT INTO assessment_definitions (code, name, description, instructions, max_score, active, created_at) VALUES
('PHQ9', 'Patient Health Questionnaire-9', 
 'A validated screening tool for depression.',
 'Over the last 2 weeks, how often have you been bothered by any of the following problems? Select the response that best describes your experience.',
 27, TRUE, NOW());

-- =====================================================
-- ASSESSMENT QUESTIONS (PHQ-9)
-- =====================================================
INSERT INTO assessment_questions (definition_id, question, display_order, option_0_text, option_1_text, option_2_text, option_3_text) VALUES
(1, 'Little interest or pleasure in doing things', 1, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Feeling down, depressed, or hopeless', 2, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Trouble falling or staying asleep, or sleeping too much', 3, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Feeling tired or having little energy', 4, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Poor appetite or overeating', 5, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Feeling bad about yourself - or that you are a failure or have let yourself or your family down', 6, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Trouble concentrating on things, such as reading the newspaper or watching television', 7, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Moving or speaking so slowly that other people could have noticed. Or the opposite - being so fidgety or restless that you have been moving around a lot more than usual', 8, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day'),
(1, 'Thoughts that you would be better off dead, or of hurting yourself', 9, 'Not at all', 'Several days', 'More than half the days', 'Nearly every day');

-- =====================================================
-- MOOD LOGS (for student1 - past 10 days)
-- =====================================================
INSERT INTO mood_logs (user_id, mood_value, note, created_at) VALUES
(5, 3, 'Feeling okay today, had a busy morning.', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, 4, 'Good day! Finished my assignment early.', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(5, 2, 'Stressed about upcoming exam.', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(5, 2, 'Still anxious. Couldn''t sleep well.', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(5, 3, 'Exam went okay. Relieved.', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 4, 'Had a nice lunch with friends.', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 5, 'Great day! Got good feedback on my project.', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 3, 'Normal day, nothing special.', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 2, 'Feeling a bit down, not sure why.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 2, 'Still feeling low.', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- ASSESSMENT RESULTS (for student1)
-- =====================================================
INSERT INTO assessment_results (user_id, definition_id, total_score, severity, responses_json, completed_at) VALUES
(5, 1, 8, 'MILD', '{"1":1,"2":1,"3":1,"4":1,"5":1,"6":1,"7":1,"8":1,"9":0}', DATE_SUB(NOW(), INTERVAL 14 DAY));

-- =====================================================
-- NUDGES (for student1)
-- =====================================================
INSERT INTO nudges (user_id, message, trigger_type, action_url, action_label, created_at) VALUES
(5, 'We noticed your mood has been low lately. Remember, it''s okay to seek support. Consider booking a counselling session.', 
 'LOW_MOOD_STREAK', '/student/appointments', 'Book Appointment', NOW()),
(5, 'It''s been 2 weeks since your last self-assessment. Taking regular assessments helps track your wellbeing.',
 'ASSESSMENT_DUE', '/student/assessment', 'Take Assessment', NOW());

-- =====================================================
-- APPOINTMENTS
-- =====================================================
INSERT INTO appointments (student_id, counsellor_id, start_at, end_at, status, reason, created_at, updated_at) VALUES
(5, 2, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 1 HOUR), 
 'SCHEDULED', 'I''ve been feeling stressed about exams and would like to talk about coping strategies.', NOW(), NOW());

-- =====================================================
-- FORUM THREADS AND POSTS
-- =====================================================
INSERT INTO forum_threads (author_id, anonymous_alias, title, content, status, created_at, updated_at) VALUES
(5, 'WellnessSeeker42', 'How do you deal with exam anxiety?',
 'Hi everyone, I have my finals coming up and I''m really struggling with anxiety. My mind goes blank when I sit down to study. Has anyone else experienced this? What helped you?',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO forum_posts (thread_id, author_id, anonymous_alias, content, status, created_at, updated_at) VALUES
(1, 6, 'CalmMind88', 'I totally understand what you''re going through. What helped me was the Pomodoro technique - study for 25 minutes, then take a 5-minute break. It made studying feel less overwhelming.',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 5, 'WellnessSeeker42', 'Thanks! I''ll try that. Did you use any apps for it?',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 6, 'CalmMind88', 'Yes, there are many free Pomodoro timer apps. Also, try the breathing exercises from the anxiety module here - they really help before study sessions!',
 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- =====================================================
-- FORUM REPORTS
-- =====================================================
INSERT INTO forum_reports (post_id, reporter_id, reason, details, status, created_at) VALUES
(3, 5, 'SPAM', 'This seems like promotional content for an app.', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =====================================================
-- REFERRALS
-- =====================================================
INSERT INTO referrals (faculty_id, student_identifier, student_id, summary, urgency, consent_given, status, created_at, updated_at) VALUES
(4, 'student2@dmhlh.test', 6, 
 'Bob has been missing classes and seems withdrawn during lectures. I''m concerned about his wellbeing. He mentioned having difficulty concentrating.', 
 'MEDIUM', TRUE, 'PENDING', NOW(), NOW());

-- =====================================================
-- FORUM SETTINGS (singleton)
-- =====================================================
INSERT INTO forum_settings (allow_posting, max_post_length, max_title_length, banned_words, require_moderation, allow_anonymous, updated_at) VALUES
(TRUE, 5000, 200, 'spam,advertisement,buy now,click here', FALSE, TRUE, NOW());

-- =====================================================
-- INTEGRATION SETTINGS
-- =====================================================
INSERT INTO integration_settings (setting_key, setting_value, is_secret, description, updated_at) VALUES
('SSO_ENABLED', 'false', FALSE, 'Enable SSO integration', NOW()),
('SSO_PROVIDER_URL', '', FALSE, 'SSO Provider URL', NOW()),
('SSO_CLIENT_ID', '', TRUE, 'SSO Client ID', NOW()),
('SSO_CLIENT_SECRET', '', TRUE, 'SSO Client Secret', NOW()),
('EMAIL_NOTIFICATIONS_ENABLED', 'false', FALSE, 'Enable email notifications', NOW()),
('SMTP_HOST', '', FALSE, 'SMTP Server Host', NOW()),
('SMTP_PORT', '587', FALSE, 'SMTP Server Port', NOW()),
('SMTP_USERNAME', '', TRUE, 'SMTP Username', NOW()),
('SMTP_PASSWORD', '', TRUE, 'SMTP Password', NOW());

-- =====================================================
-- CARE PLAN (for student1)
-- =====================================================
INSERT INTO care_plans (user_id, based_on_assessment_id, risk_level, summary, recommendations_json, created_at, updated_at) VALUES
(5, 1, 'MILD', 
 'Based on your recent PHQ-9 assessment, you''re experiencing mild symptoms. Here are some personalized recommendations to support your wellbeing.',
 '[{"title":"Daily Mood Tracking","description":"Continue logging your mood daily to identify patterns and triggers.","priority":"HIGH"},{"title":"Learning Module","description":"Complete the ''Managing Academic Stress'' module for helpful coping strategies.","priority":"MEDIUM"},{"title":"Breathing Exercises","description":"Practice deep breathing for 5 minutes each morning.","priority":"MEDIUM"},{"title":"Social Connection","description":"Reach out to a friend or family member at least once this week.","priority":"LOW"}]',
 NOW(), NOW());
