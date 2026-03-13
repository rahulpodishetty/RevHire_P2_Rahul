-- RevHire Database Setup Script for Oracle
-- Create sequences for all entities

CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE company_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE employer_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE job_seeker_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE resume_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE job_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE application_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE saved_job_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE notification_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE application_note_seq START WITH 1 INCREMENT BY 1;

-- Note: Ensure the 'users' table and others are created by Hibernate 
-- or use the following DDL if ddl-auto is not set to 'update' or 'create'.
-- (The following is handled by Spring Boot JPA automatically if configured)

-- New columns added in v2: run ONLY if tables already exist (ddl-auto = none/validate)
-- ALTER TABLE job_seekers ADD location VARCHAR2(150);
-- ALTER TABLE job_seekers ADD current_employment_status VARCHAR2(50);
-- ALTER TABLE applications ADD notes VARCHAR2(2000);

-- Fix NULL values and constraints for is_deleted column in jobs table
UPDATE jobs SET is_deleted = 0 WHERE is_deleted IS NULL;
ALTER TABLE jobs MODIFY is_deleted NUMBER(1) DEFAULT 0 NOT NULL;

COMMIT;
