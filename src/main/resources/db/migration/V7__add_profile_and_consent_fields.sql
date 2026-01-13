-- V7: Add profile fields to users and granular consent options

-- Add profile fields to users table
ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS bio TEXT,
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS student_id VARCHAR(50),
    ADD COLUMN IF NOT EXISTS faculty VARCHAR(100),
    ADD COLUMN IF NOT EXISTS notification_email BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS notification_appointment BOOLEAN NOT NULL DEFAULT TRUE;

-- Add granular consent fields to consents table
ALTER TABLE consents
    ADD COLUMN IF NOT EXISTS consent_mood_tracking BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS consent_assessment_data BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS consent_appointment_history BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS consent_ai_coach BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS consent_anonymous_analytics BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS consent_faculty_referral BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS withdrawn_at DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME;

-- Update existing consent records to have default values
UPDATE consents SET 
    consent_mood_tracking = TRUE,
    consent_assessment_data = TRUE,
    consent_appointment_history = TRUE,
    consent_ai_coach = TRUE,
    consent_anonymous_analytics = TRUE,
    consent_faculty_referral = FALSE
WHERE consent_mood_tracking IS NULL;
