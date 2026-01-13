-- DMHLH Database Schema
-- V1: Initial schema creation

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    role ENUM('STUDENT', 'COUNSELLOR', 'FACULTY', 'ADMIN') NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Consents table
CREATE TABLE consents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    accepted BOOLEAN NOT NULL,
    accepted_at DATETIME,
    ip_address VARCHAR(45),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Learning modules table
CREATE TABLE learning_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type ENUM('TEXT', 'VIDEO', 'MIXED') NOT NULL,
    body_content TEXT,
    video_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL,
    display_order INT,
    created_by BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_modules_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Quiz questions table
CREATE TABLE quiz_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    option_a VARCHAR(500) NOT NULL,
    option_b VARCHAR(500) NOT NULL,
    option_c VARCHAR(500) NOT NULL,
    option_d VARCHAR(500) NOT NULL,
    correct_option CHAR(1) NOT NULL,
    explanation TEXT,
    display_order INT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (module_id) REFERENCES learning_modules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Quiz attempts table
CREATE TABLE quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    score_percentage DOUBLE NOT NULL,
    answers_json TEXT,
    completed_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES learning_modules(id) ON DELETE CASCADE,
    INDEX idx_quiz_attempts_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assessment definitions table
CREATE TABLE assessment_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    instructions TEXT,
    max_score INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assessment questions table
CREATE TABLE assessment_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    definition_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    display_order INT NOT NULL,
    option_0_text VARCHAR(255),
    option_1_text VARCHAR(255),
    option_2_text VARCHAR(255),
    option_3_text VARCHAR(255),
    FOREIGN KEY (definition_id) REFERENCES assessment_definitions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assessment results table
CREATE TABLE assessment_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    definition_id BIGINT NOT NULL,
    total_score INT NOT NULL,
    severity ENUM('MINIMAL', 'MILD', 'MODERATE', 'MODERATELY_SEVERE', 'SEVERE') NOT NULL,
    responses_json TEXT NOT NULL,
    completed_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (definition_id) REFERENCES assessment_definitions(id) ON DELETE CASCADE,
    INDEX idx_assessment_results_user (user_id),
    INDEX idx_assessment_results_date (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mood logs table
CREATE TABLE mood_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mood_value INT NOT NULL,
    note TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_mood_logs_user (user_id),
    INDEX idx_mood_logs_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Nudges table
CREATE TABLE nudges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    trigger_type ENUM('LOW_MOOD_STREAK', 'ASSESSMENT_DUE', 'ASSESSMENT_RESULT', 'APPOINTMENT_REMINDER', 'LEARNING_SUGGESTION', 'GENERAL_WELLNESS') NOT NULL,
    action_url VARCHAR(500),
    action_label VARCHAR(100),
    seen_at DATETIME,
    dismissed_at DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_nudges_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Appointments table
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    counsellor_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    status ENUM('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') NOT NULL,
    reason TEXT,
    cancellation_reason TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (counsellor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_appointments_student (student_id),
    INDEX idx_appointments_counsellor (counsellor_id),
    INDEX idx_appointments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Counsellor notes table
CREATE TABLE counsellor_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    share_with_student BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Forum threads table
CREATE TABLE forum_threads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    anonymous_alias VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    status ENUM('ACTIVE', 'HIDDEN', 'REMOVED') NOT NULL,
    view_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_forum_threads_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Forum posts table
CREATE TABLE forum_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    anonymous_alias VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    parent_post_id BIGINT,
    status ENUM('ACTIVE', 'HIDDEN', 'REMOVED') NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (thread_id) REFERENCES forum_threads(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_post_id) REFERENCES forum_posts(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Forum reports table
CREATE TABLE forum_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT,
    thread_id BIGINT,
    reporter_id BIGINT NOT NULL,
    reason ENUM('HARASSMENT', 'SPAM', 'INAPPROPRIATE', 'MISINFORMATION', 'SELF_HARM', 'OTHER') NOT NULL,
    details TEXT,
    status ENUM('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED') NOT NULL,
    resolved_by BIGINT,
    resolution_note TEXT,
    resolved_at DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (thread_id) REFERENCES forum_threads(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_forum_reports_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Referrals table
CREATE TABLE referrals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id BIGINT NOT NULL,
    student_identifier VARCHAR(255) NOT NULL,
    student_id BIGINT,
    summary TEXT NOT NULL,
    urgency ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    consent_given BOOLEAN NOT NULL,
    status ENUM('PENDING', 'IN_REVIEW', 'CLOSED') NOT NULL,
    assigned_counsellor_id BIGINT,
    counsellor_notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (faculty_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_counsellor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_referrals_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Forum settings table (singleton)
CREATE TABLE forum_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    allow_posting BOOLEAN NOT NULL DEFAULT TRUE,
    max_post_length INT NOT NULL DEFAULT 5000,
    max_title_length INT NOT NULL DEFAULT 200,
    banned_words TEXT,
    require_moderation BOOLEAN NOT NULL DEFAULT FALSE,
    allow_anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Integration settings table
CREATE TABLE integration_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    is_secret BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT,
    actor_email VARCHAR(255),
    action_type ENUM('LOGIN', 'LOGOUT', 'CONSENT_ACCEPTED', 'CONSENT_DECLINED', 'ASSESSMENT_SUBMITTED', 'MOOD_LOGGED', 'APPOINTMENT_CREATED', 'APPOINTMENT_CANCELLED', 'APPOINTMENT_COMPLETED', 'POST_CREATED', 'POST_REPORTED', 'MODERATION_ACTION', 'REFERRAL_CREATED', 'REFERRAL_STATUS_CHANGED', 'MODULE_CREATED', 'MODULE_UPDATED', 'SETTINGS_CHANGED') NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    metadata_json TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at DATETIME NOT NULL,
    INDEX idx_audit_logs_actor (actor_id),
    INDEX idx_audit_logs_action (action_type),
    INDEX idx_audit_logs_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat messages table (for AI Coach)
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(100) NOT NULL,
    sender ENUM('USER', 'BOT') NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_messages_user (user_id),
    INDEX idx_chat_messages_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Care plans table
CREATE TABLE care_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    based_on_assessment_id BIGINT,
    risk_level ENUM('MINIMAL', 'MILD', 'MODERATE', 'MODERATELY_SEVERE', 'SEVERE') NOT NULL,
    summary TEXT,
    recommendations_json TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (based_on_assessment_id) REFERENCES assessment_results(id) ON DELETE SET NULL,
    INDEX idx_care_plans_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
